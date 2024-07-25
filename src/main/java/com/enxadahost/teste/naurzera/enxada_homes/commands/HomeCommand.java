package com.enxadahost.teste.naurzera.enxada_homes.commands;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import com.enxadahost.teste.naurzera.enxada_homes.api.HomeAPI;
import com.enxadahost.teste.naurzera.enxada_homes.objects.EnxadaHome;
import com.enxadahost.teste.naurzera.enxada_homes.utils.GetHoverStringList;
import com.enxadahost.teste.naurzera.enxada_homes.utils.HomeCooldown;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;

public class HomeCommand
    extends
    BukkitCommand
{
   // Cache de Cooldown
   static LinkedHashMap<Player, Long> cooldownPlayers = new LinkedHashMap<>();

   // Comandos dinâmicos (configuráveis na config)
   public HomeCommand(String cmdLabel)
   {
      super(cmdLabel);
   }

   // Inicio do comando
   @Override
   public boolean execute(CommandSender sender, String commandLabel, String[] args)
   {
      // Deixando em async para poupar trabalho da thread principal
      Enxada_Homes.getInstance().executor.submit(() ->
      {
         // Sincronizando para manter tudo em ordem
         synchronized ("enxada_home")
         {
            // Buscando a informação de debug
            boolean debug = Enxada_Homes.getInstance().debug;
            // Buscando a API
            HomeAPI homeAPI = Enxada_Homes.getInstance().homeAPI;
            // Verificando se o jogador é um player
            if (sender instanceof Player)
            {
               if (debug)
               {
                  Bukkit.getLogger().log(Level.INFO, "[Home Debug] O jogador é um player");
               }
               Player player = (Player) sender;
               // Buscando qual é o cooldown do jogador
               int cooldown = HomeCooldown.getCooldown(player);
               // Se o jogador tiver um cooldown igual ou menor do que 0, o código a seguir seria desnecessário
               if (cooldown > 0)
               {
                  if (debug)
                  {
                     Bukkit.getLogger().log(Level.INFO, "[Home Debug] Player tem Cooldown");
                  }
                  // Verificando se o jogador já utilizou o comando (se está no cache de cooldown)
                  if (cooldownPlayers.containsKey(player))
                  {
                     if (debug)
                     {
                        Bukkit.getLogger().log(Level.INFO, "[Home Debug] Player está no cache de cooldown");
                     }

                     // Intervalo que o jogador deve esperar em milisegundos
                     long cooldownMillis = cooldown * 1000L;

                     // Última vez que o jogador usou o comando
                     long lastTimeUsed = cooldownPlayers.get(player);

                     // Tempo (em milisegundos) desde que o jogador executou o comando pela última vez
                     long difference = System.currentTimeMillis() - lastTimeUsed;

                     // Se a diferença for menor do que o intervalo do jogador, então
                     // o tempo necessário para o cooldown ainda não foi o suficiente.
                     if (difference < cooldownMillis)
                     {
                        if (debug)
                        {
                           Bukkit.getLogger().log(Level.INFO, "Home Debug 4");
                        }
                        // Calculando o tempo restante para que o jogador possa executar novamente o comando
                        long resting = cooldownMillis - difference;

                        // (Converter segundo para milisegundos terá sempre 3 números quebrados, mesmo que sejam 000)
                        // Transformando em string e formatando para apenas 1 número quebrado
                        String secondsLeft = String.format("%.1f", resting / 1000.0);

                        // Buscando a mensagem de que o comando ainda está em tempo de recarga
                        String cooldownMessage = Enxada_Homes.getInstance()
                            .getConfig()
                            .getString("messages.cooldown");

                        // Substituindo a parte do tempo restante
                        cooldownMessage = cooldownMessage.replaceAll("\\{time}", secondsLeft);

                        // Enviando a mensagem
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldownMessage));
                        return false;
                     }
                     else
                     {
                        if (debug)
                        {
                           Bukkit.getLogger().log(Level.INFO, "[Home Debug] Substituindo o cooldown do jogador");
                        }
                        // Atualizando a última vez que o jogador usou o comando
                        cooldownPlayers.replace(player, System.currentTimeMillis());
                     }
                  }
                  else
                  {
                     if (debug)
                     {
                        Bukkit.getLogger().log(Level.INFO, "[Home Debug] Inserindo o jogador no cache de cooldown");
                     }
                     // Inserindo a última vez que o jogador usou o comando
                     cooldownPlayers.put(player, System.currentTimeMillis());
                  }
               }

               if (args.length > 0)
               {
                  if (debug)
                  {
                     Bukkit.getLogger().log(Level.INFO, "[Home Debug] Passou da verificação de cooldown");
                  }
                  String ownerName;
                  StringBuilder homeName;

                  // Verificando se o argumento 0 tem :, pois se tiver significa que irá para a home
                  // de outro jogador.
                  if (args[0].contains(":"))
                  {
                     if (debug)
                     {
                        Bukkit.getLogger().log(Level.INFO, "[Home Debug] Tem : no arg[0]");
                     }
                     try
                     {
                        if (debug)
                        {
                           Bukkit.getLogger().log(Level.INFO, "[Home Debug] Tentando buscar as informações de nome do jogador e home...");
                        }
                        // Aqui o que vem antes dos : deve ser o nome do jogador
                        ownerName = args[0].split(":")[0];

                        // E o que vem depois, o nome da home
                        homeName = new StringBuilder(args[0].split(":")[1]);

                     // Se der IndexOutOfBoundExcepiton, significa que o jogador não colocou nada depois dos :
                     } catch (IndexOutOfBoundsException exception)
                     {
                        if (debug)
                        {
                           Bukkit.getLogger().log(Level.INFO, "[Home Debug] Home não especificada.");
                        }
                        ownerName = args[0].split(":")[0];

                        /*
                        Já que não tem uma home específica, vamos listar as homes dessa pessoa
                        mas só se o jogador tiver permissão de ir até elas.
                        */

                        // Buscando as homes do jogador especificado
                        homeAPI.getHomes(ownerName);

                        // Se o jogador não tiver uma home, não tem como listar nada xD
                        if (homeAPI.hasAnyHome(ownerName))
                        {
                           /* Aqui vamos fazer algumas verificações:
                           1. Se o jogador tiver a permissão para visitar homes mesmo sem serem públicas, não necessitamos verificar
                           mais nada. É só listar todas as homes da pessoa;
                           2. Se o jogador tiver especificado o próprio nome, também iremos mostrar todas as homes;
                           3. Se não ocorrerem os casos 1 e 2, iremos listar apenas as homes públicas.
                            */
                           List<EnxadaHome> homes = sender.hasPermission(Enxada_Homes.getInstance()
                               .getConfig()
                               .getString("settings.public-exempt-permission")) || sender.getName()
                               .equalsIgnoreCase(ownerName) ? Enxada_Homes.getInstance().homeAPI.getHomes(ownerName) :
                               Enxada_Homes.getInstance().homeAPI.getHomes(ownerName)
                                   .stream()
                                   .filter(it -> it.isPublic)
                                   .toList();

                           // Se não tiver nenhuma home na lista...
                           if (homes.isEmpty())
                           {
                              sender.sendMessage("§c Opa! Este jogador não tem nenhuma home acessível por você =(");
                              return false;
                           }

                           /*
                           Aqui vamos fazer o comando clicável, assim se o jogador clicar no nome
                           da home que aparecer, ele irá para ela.
                            */

                           if (debug)
                           {
                              Bukkit.getLogger().log(Level.INFO, "[Home Debug] Criando mensagem json com a lista de homes...");
                           }

                           // TextComponent é pra poder fazer o texto ser clicável e ter alguma reação
                           TextComponent firstHome = new TextComponent(ChatColor.YELLOW + homes.get(0).homeName);

                           // Aqui a gente define o que será exibido ao passar o mouse nessa home
                           List<Content> hoverLines = GetHoverStringList.get(homes.get(0));
                           firstHome.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,hoverLines));

                           // Aqui a gente define o que será feito ao clickar
                           firstHome.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + homes.get(0).homeName));
                           TextComponent message = new TextComponent(" §7Homes: §e");
                           message.addExtra(firstHome);


                           if (homes.size() > 1)
                           {
                              for (int count = 1; count < homes.size(); count++)
                              {
                                 if (debug)
                                 {
                                    Bukkit.getLogger().log(Level.INFO, "[Home Debug] Adicionando a home "+homes.get(count).homeName+" na lista");
                                 }
                                 // Aqui fazemos a mesma coisa para cada home
                                 TextComponent otherHome = new TextComponent(ChatColor.YELLOW + homes.get(count).homeName);
                                 otherHome.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,GetHoverStringList.get(homes.get(count))));
                                 otherHome.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + homes.get(count).homeName));
                                 message.addExtra("§7, ");
                                 message.addExtra(otherHome);
                              }
                           }

                           if (debug)
                           {
                              Bukkit.getLogger().log(Level.INFO, "[Home Debug] Mensagem para o jogador: "+message.getText());
                           }
                           // Para funcionar o comando clicável tem que enviar dessa forma
                           player.spigot().sendMessage(message);
                           return true;
                        }
                        else
                        {
                           // Talvez o jogador não tenha nenhuma home...
                           sender.sendMessage("§c Opa! Não encontramos nenhuma home do jogador "+ownerName+".");
                        }
                        return false;
                     }
                  }
                  else
                  {
                     if (debug)
                     {
                        Bukkit.getLogger().log(Level.INFO, "[Home Debug] Não tem : no arg[0]");
                     }
                     // Aqui como não tem : no argumento, vamos definir que o dono da home é o próprio executor do comando
                     ownerName = player.getName();
                     homeName = new StringBuilder(args[0]);
                  }
                  // Sistema para aceitar espaços no nome da home

                  // Se tiver mais argumentos...
                  if (args.length > 1)
                  {
                     if (debug)
                     {
                        Bukkit.getLogger().log(Level.INFO, "[Home Debug] Tem espaço");
                     }
                     // Separamos cada um com um espaço
                     for (int count = 1; count < args.length; count++)
                        homeName.append(" ")
                            .append(args[count]);
                  }
                  String homeString = homeName.toString();

                  // Aqui buscamos o nome da home já com os espaços
                  EnxadaHome home = homeAPI.getHome(ownerName, homeString);

                  // Caso não seja encontrada...
                  if (home == null)
                  {
                     if (debug)
                     {
                        Bukkit.getLogger().log(Level.INFO, "[Home Debug] A home não existe");
                     }
                     sender.sendMessage("§c Opa! Home não encontrada");
                     return false;
                  }

                  // Caso não seja publica...
                  if (!home.isPublic)
                  {
                     if (debug)
                     {
                        Bukkit.getLogger().log(Level.INFO, "[Home Debug] A home não é publica");
                     }

                     // Buscando na config a permissão para acessar homes privadas de outros jogadores
                     String bypassPerm = Enxada_Homes.getInstance()
                         .getConfig()
                         .getString("settings.public-exempt-permission");

                     // Verificando se o jogador tem essa permissão
                     if (!player.hasPermission(bypassPerm))
                     {

                        // Não tem a permissão, verificando se é o dono da home...
                        if (!player.getUniqueId()
                            .toString()
                            .equals(home.ownerUuid.toString()))
                        {

                           // Não é o dono da home!
                           if (debug)
                           {
                              Bukkit.getLogger().log(Level.INFO,"[Home Debug] O jogador não pode acessar a home privada.");
                           }

                           // Buscando na config a mensagem de que a home não é publica...
                           String notPublicMessage = ChatColor.translateAlternateColorCodes('&', Enxada_Homes.getInstance()
                               .getConfig()
                               .getString("messages.not-public")
                               .replaceAll("\\{player}", home.ownerName)
                               .replaceAll("\\{home}", homeString));

                           // Enviando a mensagem para o jogador
                           player.sendMessage(notPublicMessage);
                           return false;
                        }
                     }
                  }

                  // Verificando se o jogador tem a home especificada
                  if (homeAPI.hasHome(ownerName, homeString))
                  {
                     // Tem!
                     if (debug)
                     {
                        Bukkit.getLogger().log(Level.INFO, "[Home Debug] O jogador e a home existem, iniciando teleporte...");
                     }

                     // Iniciando teleporte...
                     homeAPI.teleportToHome(player, home);
                     return true;
                  }
                  else
                  {
                     // Não tem!
                     if (debug)
                     {
                        Bukkit.getLogger().log(Level.INFO, "[Home Debug] O jogador não tem essa home.");
                     }

                     // Buscando a mensagem...
                     String notPublicMessage = ChatColor.translateAlternateColorCodes('&', Enxada_Homes.getInstance()
                         .getConfig()
                         .getString("messages.not-found")
                         .replaceAll("\\{player}", home.ownerName)
                         .replaceAll("\\{home}", homeString));

                     // Enviando a mensagem
                     player.sendMessage(notPublicMessage);
                     return false;
                  }
               }

               // Aqui será caso o jogador não escreve nada depois do comando, por exemplo apenas "/home"
               else
               {
                  if (debug)
                  {
                     Bukkit.getLogger().log(Level.INFO, "[Home Debug] Não tem argumentos (O jogador só deu /"+commandLabel+")");
                  }

                  // Se o jogador tiver qualquer home, vamos listá-las para ele
                  if (Enxada_Homes.getInstance().homeAPI.hasAnyHome(player))
                  {
                     List<EnxadaHome> homes = Enxada_Homes.getInstance().homeAPI.getHomes(player.getName());


                     /*
                     O processor de criação e envio de mensagem clicável no chat é semelhante
                     à mensagem feita anteriormente no código
                      */
                     TextComponent firstHome = new TextComponent(ChatColor.YELLOW + homes.get(0).homeName);
                     List<Content> hoverLines = GetHoverStringList.get(homes.get(0));
                     firstHome.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,hoverLines));
                     firstHome.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + homes.get(0).homeName));
                     TextComponent message = new TextComponent(" §7Homes: §e");
                     message.addExtra(firstHome);
                     if (homes.size() > 1)
                     {
                        for (int count = 1; count < homes.size(); count++)
                        {
                           if (debug)
                           {
                              Bukkit.getLogger().log(Level.INFO, "[Home Debug] Adicionando a home "+homes.get(count).homeName+" na lista");
                           }
                           TextComponent otherHome = new TextComponent(ChatColor.YELLOW + homes.get(count).homeName);
                           otherHome.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,GetHoverStringList.get(homes.get(count))));
                           otherHome.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + homes.get(count).homeName));
                           message.addExtra("§7, ");
                           message.addExtra(otherHome);
                        }
                     }
                     if (debug)
                     {
                        Bukkit.getLogger().log(Level.INFO, "[Home Debug] Mensagem para o jogador: "+message.getText());
                     }
                     player.spigot().sendMessage(message);
                     return true;
                  }
                  else
                  {
                     // O jogador não tem casa... Coitado!
                     sender.sendMessage("§c Opa! Você não tem nenhuma home =(");
                     return false;
                  }
               }
            }
            return false;
         }
      });
      return false;
   }
}
