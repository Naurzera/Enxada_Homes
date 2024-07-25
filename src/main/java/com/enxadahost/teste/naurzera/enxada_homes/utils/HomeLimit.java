package com.enxadahost.teste.naurzera.enxada_homes.utils;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import org.bukkit.entity.Player;

public class HomeLimit
{
   /*
   Verificador de limite por permissão
    */
   public static int getHomeLimit(Player player)
   {
      int limit = Enxada_Homes.getInstance()
          .getConfig()
          .getInt("settings.limit.default");
      for (String permission : Enxada_Homes.getInstance()
          .getConfig()
          .getConfigurationSection("settings.limit.permissions")
          .getKeys(false))
      {
         String permission2 = permission.replaceAll("_", ".");
         if (player.hasPermission(permission2))
         {
            int newLimit = Enxada_Homes.getInstance()
                .getConfig()
                .getInt("settings.limit.permissions." + permission);
            if (newLimit < 0) return 99999;
            if (newLimit > limit)
            {
               limit = newLimit;
            }
         }
      }
      return limit;
   }
}
