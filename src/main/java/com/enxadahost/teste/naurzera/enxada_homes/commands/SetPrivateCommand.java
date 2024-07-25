package com.enxadahost.teste.naurzera.enxada_homes.commands;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import com.enxadahost.teste.naurzera.enxada_homes.objects.EnxadaHome;
import com.enxadahost.teste.naurzera.enxada_homes.utils.GetHome;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class SetPrivateCommand
    extends
    BukkitCommand
{
   /*
   Comando configurável para tornar uma home privada
    */
   public SetPrivateCommand(String cmdLabel)
   {
      super(cmdLabel);
   }

   @Override
   public boolean execute(CommandSender sender, String commandLabel, String[] args)
   {
      // Tirando da thread principal
      Enxada_Homes.getInstance().executor.submit(() ->
      {
         // Sincronizando pra manter a ordem
         synchronized ("enxada_setprivate")
         {
            // Buscando a permissão na config
            String bypassPerm = Enxada_Homes.getInstance()
                .getConfig()
                .getString("settings.set-public-private-others");

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
            EnxadaHome home = GetHome.getHome(sender,commandLabel,args,bypassPerm);

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

            // Verificando se é publica
            if (!home.isPublic)
            {
               // É publica!
               String alreadyPublic = ChatColor.translateAlternateColorCodes('&', Enxada_Homes.getInstance()
                   .getConfig()
                   .getString("messages.already-private")
                   .replaceAll("\\{player}", home.ownerName)
                   .replaceAll("\\{home}", home.homeName));
               sender.sendMessage(alreadyPublic);
               return true;
            }

            // Se é publica...
            else
            {
               home.isPublic = false;
               String publicDefined = ChatColor.translateAlternateColorCodes('&', Enxada_Homes.getInstance()
                   .getConfig()
                   .getString("messages.private-defined")
                   .replaceAll("\\{player}", home.ownerName)
                   .replaceAll("\\{home}", home.homeName));
               sender.sendMessage(publicDefined);

               // Atualizando o jogador no banco de dados
               Enxada_Homes.getInstance().mySQL.uploadPlayer(home.ownerName);
            }
         }
         return false;
      });
      return false;
   }
}
