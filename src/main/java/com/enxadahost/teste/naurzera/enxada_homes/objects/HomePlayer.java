package com.enxadahost.teste.naurzera.enxada_homes.objects;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomePlayer
{
   /*
   Um objeto java para armazenar em cache (e facilitar o manuseio) os jogadores e suas homes
    */
   public UUID playerUuid;
   public String playerName;
   public List<EnxadaHome> playerHomes;

   public HomePlayer(Player player)
   {
      this.playerUuid = player.getUniqueId();
      this.playerName = player.getName();
      playerHomes = new ArrayList<EnxadaHome>();
   }
   public HomePlayer(UUID playerUuid, String playerName, List<EnxadaHome> playerHomes)
   {
      this.playerUuid = playerUuid;
      this.playerName = playerName;
      this.playerHomes = playerHomes;
   }

}
