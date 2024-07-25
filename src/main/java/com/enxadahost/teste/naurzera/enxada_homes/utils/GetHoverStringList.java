package com.enxadahost.teste.naurzera.enxada_homes.utils;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import com.enxadahost.teste.naurzera.enxada_homes.objects.EnxadaHome;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class GetHoverStringList
{
   /*
   Criação o hover para a mensagem json
    */
   public static List<Content> get(EnxadaHome enxadaHome)
   {
      List<String> configStrings = Enxada_Homes.getInstance().getConfig().getStringList("messages.hover-homes");
      List<Content> lines = new ArrayList<>();
      for (int index = 0;index<configStrings.size();index++)
      {
         if (Enxada_Homes.getInstance().debug)
         {
            Bukkit.getLogger().log(Level.INFO, "[HomeHover] Adidionando hover na home "+enxadaHome.homeName+" linha "+index);
         }
         lines.add(new Text(ChatColor.translateAlternateColorCodes('&', configStrings.get(index)
                 .replaceAll("\\{home}",enxadaHome.homeName)
                 .replaceAll("\\{player}",enxadaHome.ownerName)
                  )));
      }
      return lines;
   }
}
