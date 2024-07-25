package com.enxadahost.teste.naurzera.enxada_homes.listeners;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class Login
   implements
    Listener
{
   /*
   Listener de Login
   Quando o jogador entrar, ele será registrado em cache
    */
   @EventHandler
   public void onLogin(PlayerLoginEvent event)
   {
      Enxada_Homes.getInstance().homeAPI.register(event.getPlayer());
   }
}
