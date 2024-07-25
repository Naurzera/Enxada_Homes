package com.enxadahost.teste.naurzera.enxada_homes.listeners;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class Logout
   implements
    Listener
{
   /*
   Listener de logout (quit)
   Quando o jogador sair do servidor, ele será removido do cache
    */
   @EventHandler
   public void onLogin(PlayerQuitEvent event)
   {
      Enxada_Homes.getInstance().homeAPI.unregister(event.getPlayer());
   }
}
