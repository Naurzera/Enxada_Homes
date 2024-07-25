package com.enxadahost.teste.naurzera.enxada_homes.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EnxadaHome
{
   /*
   Um objeto java para armazenar em cache e facilitar o manuseio da(s) home(s) de um jogador.
    */
   public String homeName;
   public UUID ownerUuid;
   public String ownerName;
   public boolean isPublic;
   public Location location;

   public EnxadaHome(String homeName,
                     UUID ownerUuid,
                     String ownerName,
                     boolean isPublic,
                     Location location)
   {
      this.homeName = homeName;
      this.ownerUuid = ownerUuid;
      this.ownerName = ownerName;
      this.isPublic = isPublic;
      this.location = location;
   }

   public EnxadaHome(String homeName,
                     UUID ownerUuid,
                     String ownerName,
                     Location location)
   {
      this.homeName = homeName;
      this.ownerUuid = ownerUuid;
      this.ownerName = ownerName;
      this.isPublic = false;
      this.location = location;
   }

   public EnxadaHome(String homeName,
                     UUID ownerUuid,
                     Location location)
   {
      this.homeName = homeName;
      this.ownerUuid = ownerUuid;
      this.ownerName = Bukkit.getOfflinePlayer(ownerUuid)
          .getName();
      this.isPublic = false;
      this.location = location;
   }

   public EnxadaHome(String homeName,
                     Player player)
   {
      this.homeName = homeName;
      this.ownerUuid = player.getUniqueId();
      this.ownerName = player.getName();
      this.isPublic = false;
      this.location = player.getLocation();
   }

   public boolean togglePrivacity()
   {
      this.isPublic = !this.isPublic;
      return this.isPublic;
   }

}
