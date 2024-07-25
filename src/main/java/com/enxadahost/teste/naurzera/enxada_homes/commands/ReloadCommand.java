package com.enxadahost.teste.naurzera.enxada_homes.commands;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class ReloadCommand
   extends
    BukkitCommand
{
   /*
   Comando configurável para dar reload na config do plugin
    */
   public ReloadCommand(String cmdLabel)
   {
      super(cmdLabel);
   }

   @Override
   public boolean execute(CommandSender sender, String commandLabel, String[] args)
   {
      // Buscando a permissão
      String permission = Enxada_Homes.getInstance()
          .getConfig()
          .getString("settings.reload-permission");
      if (sender.hasPermission(permission))
      // Verificando a permissão
      {
         Enxada_Homes.getInstance().reload();
         sender.sendMessage("§a Sucesso! Plugin recarregado.");
         return true;
      }
      return false;
   }
}
