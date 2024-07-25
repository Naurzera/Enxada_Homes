package com.enxadahost.teste.naurzera.enxada_homes.commands;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import com.enxadahost.teste.naurzera.enxada_homes.objects.EnxadaHome;
import com.enxadahost.teste.naurzera.enxada_homes.utils.GetHome;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class TogglePrivacityCommand
    extends
    BukkitCommand
{
   /*
   Comando configurável para alternar uma home entre publica e privada
    */
   public TogglePrivacityCommand(String cmdLabel)
   {
      super(cmdLabel);
   }

   @Override
   public boolean execute(CommandSender sender, String commandLabel, String[] args)
   {
      // Tirando da thread principal
      Enxada_Homes.getInstance().executor.submit(() ->
      {
         // Sincronizando
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

            // Alterando a privadidade
            home.togglePrivacity();

            // Criando a palavra "publica" ou "privada" a partir da config e da nova privacidade da home
            String privacity = home.isPublic ? Enxada_Homes.getInstance()
                .getConfig()
                .getString("messages.public") : Enxada_Homes.getInstance()
                .getConfig()
                .getString("messages.private");

            // Criando a mensagem
            String toggled = ChatColor.translateAlternateColorCodes('&', Enxada_Homes.getInstance()
                .getConfig()
                .getString("messages.toggle-privacity")
                .replaceAll("\\{player}", home.ownerName)
                .replaceAll("\\{home}", home.homeName)
                .replaceAll("\\{privacity}", privacity));

            // Enviando a mensagem
            sender.sendMessage(toggled);

            // Atualizando o jogador no banco de dados
            Enxada_Homes.getInstance().mySQL.uploadPlayer(home.ownerName);
            return true;
         }
      });
      return false;
   }
}
