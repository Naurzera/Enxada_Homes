package com.enxadahost.teste.naurzera.enxada_homes.utils;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import org.bukkit.entity.Player;

public class HomeCooldown
{
   /*
   Verificador de cooldown por permissão
    */
   public static int getCooldown(Player player)
   {
      int cooldown = Enxada_Homes.getInstance()
          .getConfig()
          .getInt("settings.cooldown.default");
      for (String permission : Enxada_Homes.getInstance()
          .getConfig()
          .getConfigurationSection("settings.cooldown.permissions")
          .getKeys(false))
      {
         String permission2 = permission.replaceAll("_", ".");
         if (player.hasPermission(permission2))
         {
            int newCooldown = Enxada_Homes.getInstance()
                .getConfig()
                .getInt("settings.cooldown.permissions." + permission);
            if (newCooldown < cooldown)
            {
               cooldown = newCooldown;
            }
         }
      }
      return cooldown;
   }
}
