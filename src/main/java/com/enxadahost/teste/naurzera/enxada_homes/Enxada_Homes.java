package com.enxadahost.teste.naurzera.enxada_homes;

import com.enxadahost.teste.naurzera.enxada_homes.api.HomeAPI;
import com.enxadahost.teste.naurzera.enxada_homes.commands.*;
import com.enxadahost.teste.naurzera.enxada_homes.database.MySQL;
import com.enxadahost.teste.naurzera.enxada_homes.listeners.Login;
import com.enxadahost.teste.naurzera.enxada_homes.listeners.Logout;
import com.enxadahost.teste.naurzera.enxada_homes.utils.JsonEnxadaHome;
import com.enxadahost.teste.naurzera.enxada_homes.utils.JsonHomePlayer;
import com.enxadahost.teste.naurzera.enxada_homes.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class Enxada_Homes
    extends
    JavaPlugin
{
   /*
   debug: para saber se devemos enviar as mensagens de debug do plugin
   executor: para rodar em async
   executorDelay: para rodas as coisas depos de um tempo (delay do teleporte)
   instance: instanciar a main, para pegar os objetos publicos
    */
   public boolean debug;
   public ExecutorService executor;
   public ScheduledExecutorService executorDelay;
   private static Enxada_Homes instance;

   public static Enxada_Homes getInstance()
   {
      return instance;
   }

   /*
   locationUtils: transformar location em string e visse-versa
   jsonEnxadaHome: serializer/deseralizer do objeto EnxadaHome
   jsonHomePlayer: serializer/deseralizer do objeto HomePlayer
   homeAPI: API do plugin, sendo usada aqui e podendo ser usada por terceiros futuramente
   mySQL: conexão com banco de dados mysql
    */
   public LocationUtils locationUtils;
   public JsonEnxadaHome jsonEnxadaHome;
   public JsonHomePlayer jsonHomePlayer;
   public HomeAPI homeAPI;
   public MySQL mySQL;

   /*
   Lógica de inicialização do plugin
    */
   @Override
   public void onEnable()
   {
      // Definindo os objetos
      executor = Executors.newFixedThreadPool(2);
      executorDelay = Executors.newSingleThreadScheduledExecutor();
      instance = this;
      saveDefaultConfig();
      debug = getConfig().getBoolean("settings.debug");
      locationUtils = new LocationUtils();
      jsonEnxadaHome = new JsonEnxadaHome();
      jsonHomePlayer = new JsonHomePlayer();
      homeAPI = new HomeAPI();
      mySQL = new MySQL();

      // Buscando as informações dos comandos da config
      for (String command : getConfig().getString("settings.commands.home")
          .split(","))
      {
         registerCommand(new HomeCommand(command));
      }
      for (String command : getConfig().getString("settings.commands.sethome")
          .split(","))
      {
         registerCommand(new SetHomeCommand(command));
      }
      for (String command : getConfig().getString("settings.commands.setpublic")
          .split(","))
      {
         registerCommand(new SetPublicCommand(command));
      }
      for (String command : getConfig().getString("settings.commands.setprivate")
          .split(","))
      {
         registerCommand(new SetPrivateCommand(command));
      }
      for (String command : getConfig().getString("settings.commands.toggleprivacity")
          .split(","))
      {
         registerCommand(new TogglePrivacityCommand(command));
      }
      for (String command : getConfig().getString("settings.commands.reload")
          .split(","))
      {
         registerCommand(new ReloadCommand(command));
      }

      // Registrando os listeners de login e logout
      getServer().getPluginManager()
          .registerEvents(new Login(), this);
      getServer().getPluginManager()
          .registerEvents(new Logout(), this);

      // Caso o plugin seja recarregado, aqui vamos registrar todos os jogadores online para evitar bugs
      for (Player player : Bukkit.getOnlinePlayers()) homeAPI.register(player);
   }

   /*
   Reload do plugin, utilizado pelo comando de reload
    */
   public void reload()
   {
      reloadConfig();
      debug = getConfig().getBoolean("settings.debug");
      homeAPI.reload();
      mySQL = new MySQL();
   }

   /*
   Método para registrar comandos dinâmicos
    */
   private void registerCommand(BukkitCommand bukkitCommand)
   {
      try
      {
         final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         bukkitCommandMap.setAccessible(true);
         CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
         commandMap.register(bukkitCommand.getLabel(), bukkitCommand);
      }
      catch (Exception e)
      {
         setEnabled(false);
         throw new RuntimeException("Failed to register commands", e);
      }
   }
}
