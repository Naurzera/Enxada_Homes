package com.enxadahost.teste.naurzera.enxada_homes.api;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import com.enxadahost.teste.naurzera.enxada_homes.objects.EnxadaHome;
import com.enxadahost.teste.naurzera.enxada_homes.objects.HomePlayer;
import com.enxadahost.teste.naurzera.enxada_homes.utils.HomeDelay;
import com.enxadahost.teste.naurzera.enxada_homes.utils.HomeLimit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class HomeAPI
{
   /*
   Jogadores em cache
    */
   private List<HomePlayer> homePlayers;

   /*
   Sempre é bom colocar mensagens de debug em locais estratégicos do código,
   nesse caso temos na API, no comando de home e no comando de set home.
    */
   boolean debug;

   /*
   Lógica de inicialização da api
    */
   public HomeAPI()
   {
      homePlayers = new ArrayList<>();
      debug = Enxada_Homes.getInstance().debug;
   }
   /*
   Lógica de reload da api
    */
   public void reload()
   {
      debug = Enxada_Homes.getInstance().debug;
   }

   /*
   Buscando a informação de um player no cache tendo a informação do uuid
    */
   public HomePlayer getPlayer(UUID player)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Buscando jogador (1)");
      }
      Optional<HomePlayer> homePlayer = homePlayers.stream().filter(it -> it.playerUuid.toString().equals(player.toString())).findFirst();
      return homePlayer.orElse(null);
   }

   /*
   Buscando a informação de um player no cache tendo a informação do player
    */
   public HomePlayer getPlayer(Player player)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Buscando jogador (2)");
      }
      Optional<HomePlayer> homePlayer = homePlayers.stream().filter(it -> it.playerUuid.toString().equals(player.getUniqueId().toString())).findFirst();
      return homePlayer.orElse(null);
   }

   /*
   Buscando a informação de um player no cache tendo a informação do uuid
    */
   public HomePlayer getPlayer(String player)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Buscando jogador (3)");
      }
      Optional<HomePlayer> homePlayer = homePlayers.stream().filter(it -> it.playerName.equalsIgnoreCase(player)).findFirst();
      return homePlayer.orElse(null);
   }

   /*
   Registrando um jogador no cache.

   Também verificamos se ele já não está registrado no cache, pois
   talvez um jogador pode ter visitado alguma home dele, assim carregando
   ele no cache.
    */
   public void register(Player player)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Registrando o jogador em cache (1)");
      }
      if (homePlayers.stream().anyMatch(it -> it.playerName.equalsIgnoreCase(player.getName()))) return;
      HomePlayer homePlayer = Enxada_Homes.getInstance().mySQL.downloadPlayer(player.getUniqueId());
      if (homePlayer == null)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] O jogador não existe no banco de dados, criando um novo.");
         homePlayer = new HomePlayer(player);
      }
      if (!homePlayers.contains(homePlayer))
      {
         homePlayers.add(homePlayer);
      }
   }

   /*
   Registrando um jogador no cache a partir de seu nick
    */
   public boolean register(String player)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Registrando o jogador em cache (2)");
      }
      if (homePlayers.stream().anyMatch(it -> it.playerName.equalsIgnoreCase(player))) return true;
      HomePlayer homePlayer = Enxada_Homes.getInstance().mySQL.downloadPlayer(player);
      if (homePlayer == null)
      {
         return false;
      }
      if (!homePlayers.contains(homePlayer))
      {
         homePlayers.add(homePlayer);
      }
      return true;
   }

   /*
   Removendo um jogador do cache.

   Mesmo que quando outros jogadores busquem suas homes (se estiver offline) e o servidor
   carregue seus dados em cache quando isso for feito, a grande maioria dos jogadores
   não terá homes públicas e nem irá divulgá-las, então da pra "economizar" cache.
    */
   public void unregister(Player player)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Desregistrando o jogador do cache");
      }
      Optional<HomePlayer> homePlayer = homePlayers.stream()
          .filter(it -> it.playerUuid == player.getUniqueId())
          .findFirst();
      homePlayer.ifPresent(homePlayer1 -> homePlayers.remove(homePlayer1));
   }

   /*
   Buscando um Objeto EnxadaHome a partir do nome de um jogador e o nome da sua home

   Mesmo que não encontre o jogador pelo nome, vamos buscar também as iniciais, assim
   por exemplo digitando o nome "Naur:loja" poderá ir para a home de "Naurzera:loja".
    */
   public EnxadaHome getHome(String playerName, String homeName)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Buscando a home de um jogador");
      }
      Optional<HomePlayer> homePlayer = homePlayers.stream()
          .filter(hp -> hp.playerName
              .equalsIgnoreCase(playerName))
          .findFirst();
      if (homePlayer.isEmpty()) homePlayer = homePlayers.stream()
          .filter(hp -> hp.playerName.toLowerCase().startsWith(playerName.toLowerCase()))
          .findFirst();
      if (homePlayer.isEmpty())
      {
         if (debug)
         {
            Bukkit.getLogger().log(Level.INFO,"[API Debug] O jogador não estava em cache, buscando no banco de dados...");
         }
         if (!register(playerName)) return null;
         homePlayer = homePlayers.stream()
             .filter(hp -> hp.playerName
                 .equalsIgnoreCase(playerName))
             .findFirst();
         if (homePlayer.isEmpty()) homePlayer = homePlayers.stream()
             .filter(hp -> hp.playerName.toLowerCase().startsWith(playerName.toLowerCase()))
             .findFirst();
      }
      if (homePlayer.isEmpty()) return null;
      Optional<EnxadaHome> enxadaHome = homePlayer.get().playerHomes.stream().filter(it -> it.homeName.equalsIgnoreCase(homeName)).findFirst();
      if (enxadaHome.isEmpty()) return null;
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] O jogador está em cache");
      }
      return enxadaHome.get();
   }

   /*
   Buscando todas as homes de um jogador a partir de seu nome
    */
   public List<EnxadaHome> getHomes(String playerName)
   {
      List<EnxadaHome> playerHomes = new ArrayList<>();
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Buscando as homes do jogador");
      }
      Optional<HomePlayer> homePlayer = homePlayers.stream()
          .filter(hp -> hp.playerName
              .equalsIgnoreCase(playerName))
          .findFirst();
      if (homePlayer.isEmpty())
      {
         if (debug)
         {
            Bukkit.getLogger().log(Level.INFO,"[API Debug] O jogador não estava em cache, buscando no banco de dados...");
         }
         // Mesmo que não esteja em cache, vamos tentar recuperá-lo do banco de dados
         if (!register(playerName))
         {
            // Refazendo a verificação no cache depois de tentar importar do banco de dados
            homePlayer = homePlayers.stream()
                .filter(hp -> hp.playerName
                    .equalsIgnoreCase(playerName))
                .findFirst();
            homePlayer.ifPresent(player -> playerHomes.addAll(player.playerHomes));
            return playerHomes;
         }
      }
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] O jogador estava em cache");
      }
      playerHomes.addAll(homePlayer.get().playerHomes);
      return playerHomes;
   }

   /*
   Definindo uma nova home para o jogador
    */
   public boolean setHome(Player player, String homeName)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Tentando definir a home");
      }
      UUID playerUuid = player.getUniqueId();
      Optional<HomePlayer> homePlayer = homePlayers.stream()
          .filter(hp -> hp.playerUuid.toString().equals(playerUuid.toString()))
          .findFirst();
      HomePlayer homePlayer1 = homePlayer.orElse(new HomePlayer(player));
      if (homePlayer.isEmpty())
      {
         if (debug)
         {
            Bukkit.getLogger().log(Level.INFO,"[API Debug] O jogador não estava em cache");
         }
         homePlayers.add(homePlayer1);
      }
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Definindo a localização da home");
      }
      Location location = player.getLocation().add(0,0,0).clone();
      EnxadaHome enxadaHome = new EnxadaHome(homeName, playerUuid, player.getName(), false, location);
      homePlayer1.playerHomes.add(enxadaHome);
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Definindo a home "+homeName+" para "+player.getName());
      }
      Enxada_Homes.getInstance().mySQL.uploadPlayer(player,homePlayer1);
      return true;
   }

   /*
   Verificando se o jogador tem alguma home tendo a informação de seu uuid
    */
   public boolean hasAnyHome(Player player)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Verificando se o jogador tem alguma home (1)");
      }
      Optional<HomePlayer> homePlayer = homePlayers.stream()
          .filter(hp -> hp.playerUuid.toString().equals(player.getUniqueId().toString()))
          .findFirst();
      return homePlayer.isPresent();
   }

   /*
   Verificando se o jogador tem alguma home tendo a informação de seu nick
    */
   public boolean hasAnyHome(String player)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Verificando se o jogador tem alguma home (2)");
      }
      Optional<HomePlayer> homePlayer = homePlayers.stream()
          .filter(hp -> hp.playerName.equalsIgnoreCase(player))
          .findFirst();
      return homePlayer.isPresent();
   }

   /*
   Verificando se o jogador tem tem uma home específica tendo a informação de seu uuid
    */
   public boolean hasHome(Player player, String homeName)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Verificando se o jogador tem uma home com esse nome");
      }
      Optional<HomePlayer> homePlayer = homePlayers.stream()
          .filter(hp -> hp.playerUuid.toString().equals(player.getUniqueId().toString()))
          .findFirst();
      if (homePlayer.isEmpty())
      {
         if (debug)
         {
            Bukkit.getLogger().log(Level.INFO,"[API Debug] Não tem uma home com esse nome");
         }
         return false;
      }
      Optional<EnxadaHome> enxadaHome = homePlayer.get().playerHomes.stream().filter(it -> it.homeName.equalsIgnoreCase(homeName)).findFirst();
      if (enxadaHome.isEmpty()) return false;
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Fim da verificação");
      }
      return true;
   }

   /*
   Verificando se o jogador tem tem uma home específica tendo a informação de seu nick
    */
   public boolean hasHome(String playerName, String homeName)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Verificando se o jogador tem uma home com esse nome");
      }
      Optional<HomePlayer> homePlayer = homePlayers.stream()
          .filter(hp -> hp.playerName
              .equalsIgnoreCase(playerName))
          .findFirst();
      if (homePlayer.isEmpty()) homePlayer = homePlayers.stream()
          .filter(hp -> hp.playerName.toLowerCase().startsWith(playerName.toLowerCase()))
          .findFirst();
      if (homePlayer.isEmpty())
      {
         return false;
      }
      Optional<EnxadaHome> enxadaHome = homePlayer.get().playerHomes.stream().filter(it -> it.homeName.equalsIgnoreCase(homeName)).findFirst();
      if (enxadaHome.isEmpty()) return false;
      return true;
   }

   /*
   Verificando se o jogador pode criar uma home ou ultrapassa seu limite
    */
   public boolean canSetNewHome(Player player)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Verificando se o jogador não atingiu seu limite de homes definidas");
      }
      int homeLimit = HomeLimit.getHomeLimit(player);
      int actualAmount = getPlayer(player).playerHomes.size();
      if (getPlayer(player) == null) return false;
      return homeLimit>actualAmount;
   }

   /*
   Verificando se o jogador pode criar uma home com esse nome específico
    */
   public boolean canSetHomeName(Player player, String homeName)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Verificando se o jogador pode definir uma home com esse nome");
      }
      HomePlayer homePlayer = getPlayer(player);
      if (homePlayer==null) return false;
      return !hasHome(player,homeName);
   }

   /*
   Lógica de teleporte
    */
   public void teleportToHome(final Player playerToTeleport, final EnxadaHome home)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Iniciando procedimento de teleporte");
      }
      /*
      Quando vamos trabalhar com tarefas agendadas, sempre é bom utilizar
      elementos finais para garantir que eles não vão ser alterados antes
      da tarefa ser efetivamente executada.

      Vamos começar buscando a informação do tempo que o jogador deve esperar antes de ser teleporado.
       */
      final int delay = HomeDelay.getDelay(playerToTeleport);

      // Se o delay for menor que 1, não há necessidade de delay
      if (delay < 1)
      {
         if (debug)
         {
            Bukkit.getLogger().log(Level.INFO,"[API Debug] O delay é menor que 1, teleportando imediatamente");
         }
         teleport(playerToTeleport, home);
         return;
      }

      // Aqui estamos verificando na config se o plugin deve ou não impedir o teleporte caso o jogador se mova
      final boolean moveCancel = Enxada_Homes.getInstance()
          .getConfig()
          .getBoolean("settings.teleport.movement-cancel.enable");
      final Location oldLocation = playerToTeleport.getLocation();

      String teleportingMessage = ChatColor.translateAlternateColorCodes('&', Enxada_Homes.getInstance()
          .getConfig()
          .getString("messages.teleporting")
          .replaceAll("\\{time}",delay+"")
          .replaceAll("\\{home}", home.homeName)
          .replaceAll("\\{player}", home.ownerName));
      playerToTeleport.sendMessage(teleportingMessage);

      // Aqui criamos uma tarefa agendada usando a thread que criamos quando o plugin foi iniciado
      ScheduledExecutorService executor = Enxada_Homes.getInstance().executorDelay;
      executor.schedule(() ->
          {
             // Verificando se o jogador não podia se mexer...
             if (moveCancel)
             {
                if (debug)
                {
                   Bukkit.getLogger().log(Level.INFO,"[API Debug] O jogador não podia se mover");
                }
                // Buscando a informação da distância que o jogador poderia se deslocar antes do teleporte
                double distanceToCancel = Enxada_Homes.getInstance()
                    .getConfig()
                    .getDouble("settings.teleport.movement-cancel.distance-to-cancel");
                Location actualLocation = playerToTeleport.getLocation();

                // Comparando a distância, caso ultrapasse o permitido, o teleporte é cancelado
                double distance = oldLocation.distance(actualLocation);
                if (distance > distanceToCancel)
                {
                   if (debug)
                   {
                      Bukkit.getLogger().log(Level.INFO,"[API Debug] O jogador se distanciou mais do que o permitido");
                   }
                   String moveMessage = Enxada_Homes.getInstance()
                       .getConfig()
                       .getString("messages.moved");
                   moveMessage = ChatColor.translateAlternateColorCodes('&', moveMessage);
                   playerToTeleport.sendMessage(moveMessage);
                   return;
                }
                teleport(playerToTeleport, home);
                return;
             }
             teleport(playerToTeleport, home);
          }
          , delay, TimeUnit.SECONDS);
   }

   /*
   Teleportar o jogador, aplicar efeitos e etc
    */
   private void teleport(Player player, EnxadaHome home)
   {
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Iniciando teleporte");
      }
      // Teleportando
      // Aqui temos que levar tudo para a thread principal, pois envolvem mecânicas de mundo
      new BukkitRunnable()
      {
         @Override
         public void run()
         {
            player.playSound(player, Sound.valueOf(Enxada_Homes.getInstance().getConfig().getString("settings.teleport.sound")),1F,1F);
            Effect effect = Effect.valueOf(Enxada_Homes.getInstance().getConfig().getString("settings.teleport.particles"));
            player.getLocation().getWorld().playEffect(player.getLocation(), effect,0,6);
            home.location.getWorld().playEffect(home.location, effect,0,6);
            player.teleport(home.location, PlayerTeleportEvent.TeleportCause.PLUGIN);
         }
      }.runTask(Enxada_Homes.getInstance());

      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Carregando titulo");
      }
      
      // Pegando as informações da config (como titulo e subtitulo são String, já vamos formatá-los;
      // Já a mensagem enviada no chat é uma StringList, então vamos formatá-la na hora de enviar.
      String title = ChatColor.translateAlternateColorCodes('&', Enxada_Homes.getInstance()
          .getConfig()
          .getString("settings.teleport.title")
          .replaceAll("\\{home}", home.homeName)
          .replaceAll("\\{player}", home.ownerName));
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Carregando subtitulo");
      }
      String subtitle = ChatColor.translateAlternateColorCodes('&', Enxada_Homes.getInstance()
          .getConfig()
          .getString("settings.teleport.subtitle")
          .replaceAll("\\{home}", home.homeName)
          .replaceAll("\\{player}", home.ownerName));
      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Carregando mensagens de teleporte");
      }
      List<String> messageLines = Enxada_Homes.getInstance()
          .getConfig()
          .getStringList("settings.teleport.message");

      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Enviando mensagens de teleporte");
      }
      // Formatando e enviando as mensagens
      for (String line : messageLines)
      {
         player.sendMessage(ChatColor.translateAlternateColorCodes('&',
             line.replaceAll("\\{home}", home.homeName)
                 .replaceAll("\\{player}", home.ownerName)));
      }

      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Enviando titulo de teleporte");
      }
      // Enviando titulo e subtitulo
      player.sendTitle(title, subtitle, 10, 40, 10);

      if (debug)
      {
         Bukkit.getLogger().log(Level.INFO,"[API Debug] Fim do método de teleporte");
      }
   }
   
   /*
   Deletar a home de um jogador
    */
   public void deleteHome(EnxadaHome enxadaHome)
   {
      UUID ownerUuid = enxadaHome.ownerUuid;
      HomePlayer homePlayer = getPlayer(ownerUuid);
      homePlayer.playerHomes.remove(enxadaHome);
      Enxada_Homes.getInstance().mySQL.uploadPlayer(Bukkit.getOfflinePlayer(ownerUuid),homePlayer);
   }

}
