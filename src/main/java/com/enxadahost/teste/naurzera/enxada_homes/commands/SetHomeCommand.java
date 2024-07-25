package com.enxadahost.teste.naurzera.enxada_homes.commands;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import com.enxadahost.teste.naurzera.enxada_homes.api.HomeAPI;
import com.enxadahost.teste.naurzera.enxada_homes.utils.HomeLimit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class SetHomeCommand
    extends
    BukkitCommand
{
   /*
   Comando configurável para definir uma home
    */
   public SetHomeCommand(String cmdLabel)
   {
      super(cmdLabel);
   }

   @Override
   public boolean execute(CommandSender sender, String commandLabel, String[] args)
   {
      // Levando para uma thread alternativa
      Enxada_Homes.getInstance().executor.submit(() ->
      {
         // Sincronizando para manter em ordem
         synchronized ("enxada_sethome")
         {
            // Buscando informação de debug
            boolean debug = Enxada_Homes.getInstance().debug;
            if (debug)
            {
               Bukkit.getLogger().log(Level.INFO,"[Sethome Debug] Debug está ativo");
            }

            // Buscando API
            HomeAPI homeAPI = Enxada_Homes.getInstance().homeAPI;

            // Verificando se o executor do comando é um Player
            if (sender instanceof Player)
            {
               if (debug)
               {
                  Bukkit.getLogger().log(Level.INFO,"[Sethome Debug] O executor é um jogador");
               }

               // Definindo player
               Player player = (Player) sender;

               // Verificando se o jogador pode definir uma nova home (ou já atingiu seu limite)
               if (homeAPI.canSetNewHome(player))
               {
                  if (debug)
                  {
                     Bukkit.getLogger().log(Level.INFO, "[Sethome Debug] O jogador pode definir uma nova home");
                  }

                  // Verificando se o jogador especificou o nome da nova home
                  if (args.length > 0)
                  {
                     if (debug)
                     {
                        Bukkit.getLogger().log(Level.INFO, "[Sethome Debug] O jogador especificou o nome da nova home");
                     }

                     // Definindo o nome da home com StringBuilder para administrar o String e os espaços,
                     // já que o nome da home pode conter espaços.
                     StringBuilder homeName = new StringBuilder(args[0]);
                     if (args.length > 1)
                     {
                        if (debug)
                        {
                           Bukkit.getLogger().log(Level.INFO, "[Sethome Debug] O nome da home contém espaços");
                        }
                        for (int count = 1; count < args.length; count++)
                           homeName.append(" ")
                               .append(args[count]);
                     }

                     // Para prevenir qualquer problema futuro, vamos bloquear o nome da home ter :
                     if (homeName.toString()
                         .contains(":"))
                     {
                        if (debug)
                        {
                           Bukkit.getLogger().log(Level.INFO, "[Sethome Debug] O nome da home contém :");
                        }
                        sender.sendMessage("§c Opa! A home não pode conter dois pontos no nome.");
                        return false;
                     }

                     // Vamos ver se o jogador já não tem uma home com esse nome...
                     if (homeAPI.canSetHomeName(player, homeName.toString()))
                     {
                        if (debug)
                        {
                           Bukkit.getLogger().log(Level.INFO, "[Sethome Debug] O jogador não tem uma home com esse nome");
                        }

                        // Definindo a home
                        if (homeAPI.setHome(player, homeName.toString()))
                        {
                           if (debug)
                           {
                              Bukkit.getLogger().log(Level.INFO,"[Sethome Debug] Home definida");
                           }

                           // Enviando mensagem de que a home foi definida, de acordo com a config
                           player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                   Enxada_Homes.getInstance()
                                       .getConfig()
                                       .getString("messages.defined"))
                               .replaceAll("\\{home}", homeName.toString()));
                           return true;
                        }
                        else return false;
                     }

                     // Se tiver uma home com esse nome...
                     else
                     {
                        if (debug)
                        {
                           Bukkit.getLogger().log(Level.INFO, "[Sethome Debug] O jogador já tem uma home com esse nome");
                        }

                        // Enviando a mensagem de que ja tem uma home com esse nome, de acordo com a config
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Enxada_Homes.getInstance()
                                    .getConfig()
                                    .getString("messages.already-have"))
                            .replaceAll("\\{home}", homeName.toString()));
                        return false;
                     }
                  }

                  // Caso o jogador não tenha especificado um nome para a home...
                  else
                  {
                     if (debug)
                     {
                        Bukkit.getLogger().log(Level.INFO, "[Sethome Debug] O jogador não especificou o nome da home");
                     }
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                         Enxada_Homes.getInstance()
                             .getConfig()
                             .getString("messages.no-home-name")));
                     return false;
                  }
               }

               // Caso o jogador já tenha atingido o seu limite de homes...
               else
               {
                  if (debug)
                  {
                     Bukkit.getLogger().log(Level.INFO, "[Sethome Debug] O jogador atingiu seu limite de homes");
                  }
                  player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                      Enxada_Homes.getInstance()
                          .getConfig()
                          .getString("messages.home-limit")
                          .replaceAll("\\{amount}",""+HomeLimit.getHomeLimit(player))));
                  return false;
               }
            }

            // Caso o executor do comando não seja um player (só pode ser o console... Aquele pestinha)
            else
            {
               if (debug)
               {
                  Bukkit.getLogger().log(Level.INFO,"[Sethome Debug] O comando foi executado pelo console");
               }
               sender.sendMessage(" Opa! O console não pode definir homes xD");
               return false;
            }
         }
      });
      return false;
   }
}
