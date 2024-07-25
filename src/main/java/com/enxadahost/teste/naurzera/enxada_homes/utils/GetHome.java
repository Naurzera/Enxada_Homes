package com.enxadahost.teste.naurzera.enxada_homes.utils;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import com.enxadahost.teste.naurzera.enxada_homes.api.HomeAPI;
import com.enxadahost.teste.naurzera.enxada_homes.objects.EnxadaHome;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetHome
{
   /*
   Como três comandos diferentes (SetPrivate, SetPublic e TogglePrivacity) usam esse código, preferi criar um método para não repetí-lo
    */
   public static EnxadaHome getHome(CommandSender sender, String commandLabel, String[] args, String permission)
   {
      HomeAPI homeAPI = Enxada_Homes.getInstance().homeAPI;
      if (args.length > 0)
      {
         String ownerName;
         StringBuilder homeName;
         if (args[0].contains(":"))
         {
            try
            {
               ownerName = args[0].split(":")[0];
               homeName = new StringBuilder(args[0].split(":")[1]);
            } catch (IndexOutOfBoundsException exception)
            {
               sender.sendMessage("§c Opa! Utilize (player):(home)");
               return null;
            }
         }
         else
         {
            if (!(sender instanceof Player))
            {
               sender.sendMessage("§c Opa! Uso incorreto do comando.");
               return null;
            }
            Player player = (Player) sender;
            ownerName = player.getName();
            homeName = new StringBuilder(args[0]);
         }
         if (args.length > 1)
         {
            for (int count = 1; count < args.length; count++)
               homeName.append(" ")
                   .append(args[count]);
         }
         String homeString = homeName.toString();
         EnxadaHome home = homeAPI.getHome(ownerName, homeString);
         if (home == null)
         {
            sender.sendMessage("§c Opa! Home não encontrada");
            return null;
         }
         boolean canChange = (sender instanceof Player && (((Player) sender).getUniqueId() == home.ownerUuid) || sender.hasPermission(permission));
         if (!canChange)
         {
            String noPermissionToChange = ChatColor.translateAlternateColorCodes('&', Enxada_Homes.getInstance()
                .getConfig()
                .getString("messages.no-permission-change")
                .replaceAll("\\{player}", home.ownerName)
                .replaceAll("\\{home}", homeString));
            sender.sendMessage(noPermissionToChange);
            return null;
         }
         if (homeAPI.hasHome(ownerName, homeString))
         {
            return home;
         }
      }
      return null;
   }
}
