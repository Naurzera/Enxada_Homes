package com.enxadahost.teste.naurzera.enxada_homes.utils;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import org.bukkit.entity.Player;

public class HomeDelay
{
   /*
   Verificador de delay por permissão
    */
   public static int getDelay(Player player)
   {
      int delay = Enxada_Homes.getInstance()
          .getConfig()
          .getInt("settings.delay.default");
      for (String permission : Enxada_Homes.getInstance()
          .getConfig()
          .getConfigurationSection("settings.delay.permissions")
          .getKeys(false))
      {
         String permission2 = permission.replaceAll("_", ".");
         if (player.hasPermission(permission2))
         {
            int newDelay = Enxada_Homes.getInstance()
                .getConfig()
                .getInt("settings.delay.permissions." + permission);
            if (newDelay < delay)
            {
               delay = newDelay;
            }
         }
      }
      return delay;
   }
}
