package com.enxadahost.teste.naurzera.enxada_homes.commands;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import com.enxadahost.teste.naurzera.enxada_homes.objects.EnxadaHome;
import com.enxadahost.teste.naurzera.enxada_homes.utils.GetHome;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class DelHomeCommand
    extends
    BukkitCommand
{
   /*
   Comando configurável para deletar uma home
    */
   public DelHomeCommand(String cmdLabel)
   {
      super(cmdLabel);
   }

   @Override
   public boolean execute(CommandSender sender, String commandLabel, String[] args)
   {
      // Tirando da thread principal
      Enxada_Homes.getInstance().executor.submit(() ->
      {
         // Sincronizando para deixar em ordem
         synchronized ("enxada_delhome")
         {
            // Buscando a permissão na config
            String bypassPerm = Enxada_Homes.getInstance()
                .getConfig()
                .getString("settings.delete-other-homes");

            // Parando por aqui caso o jogador não tenha a permissão
            if (!sender.hasPermission(bypassPerm))
            {
               String noPermissionToChange = ChatColor.translateAlternateColorCodes('&', Enxada_Homes.getInstance()
                   .getConfig()
                   .getString("messages.no-permission-change"));
               sender.sendMessage(noPermissionToChange);
               return false;
            }

            // Buscando a home do comando
            EnxadaHome home = GetHome.getHome(sender, commandLabel, args, bypassPerm);

            // Verificando se é nula
            if (home == null)
            {
               // Buscando a mensagem...
               String notPublicMessage = ChatColor.translateAlternateColorCodes('&', Enxada_Homes.getInstance()
                   .getConfig()
                   .getString("messages.not-found")
                   .replaceAll("\\{player}", home.ownerName));

               // Enviando a mensagem
               sender.sendMessage(notPublicMessage);
               return false;
            }

            // Deletando
            Enxada_Homes.getInstance().homeAPI.deleteHome(home);

            // Buscando a mensagem...
            String deletedMessage = ChatColor.translateAlternateColorCodes('&', Enxada_Homes.getInstance()
                .getConfig()
                .getString("messages.deleted")
                .replaceAll("\\{player}", home.ownerName)
                .replaceAll("\\{home}", home.homeName));
            sender.sendMessage(deletedMessage);
         }
         return false;
      });
      return false;
   }
}
