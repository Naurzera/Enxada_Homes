package com.enxadahost.teste.naurzera.enxada_homes.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtils
{
   /*
   Transformar location em string
    */
   public String locationToString(Location location)
   {
      String world = location.getWorld().getName();
      String x = String.format("%.1f", location.getX()).replaceAll(",",".");
      String y = String.format("%.1f", location.getY()).replaceAll(",",".");
      String z = String.format("%.1f", location.getZ()).replaceAll(",",".");
      String yaw = String.format("%.1f", location.getYaw()).replaceAll(",",".");
      String pitch = String.format("%.1f", location.getPitch()).replaceAll(",",".");
      return world+";"+x+";"+y+";"+z+";"+yaw+";"+pitch;
   }

   /*
   Transformar string em location
    */
   public Location stringToLocation(String string)
   {
      String[] infos = string.split(";");
      World world = Bukkit.getWorld(infos[0]);
      double x = Double.parseDouble(infos[1]);
      double y = Double.parseDouble(infos[2]);
      double z = Double.parseDouble(infos[3]);
      float yaw = Float.parseFloat(infos[4]);
      float pitch = Float.parseFloat(infos[5]);
      return new Location(world,x,y,z,yaw,pitch);
   }
}
