package com.enxadahost.teste.naurzera.enxada_homes.database;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import com.enxadahost.teste.naurzera.enxada_homes.objects.HomePlayer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class MySQL
{
   // Variáveis locais para fazer a conexão
   private String url, user, pass, host, port, database;
   // Conexão
   private Connection connection;

   // Quando o objeto é criado, buscamos as informações na config, fazemos a conexão e criamos a tabela.
   public MySQL()
   {
      this.database = Enxada_Homes.getInstance()
          .getConfig()
          .getString("storage.mysql.database");
      this.host = Enxada_Homes.getInstance()
          .getConfig()
          .getString("storage.mysql.hostname")
          .split(":")[0];
      try
      {
         this.port = Enxada_Homes.getInstance()
             .getConfig()
             .getString("storage.mysql.hostname")
             .split(":")[1];
      } catch (IndexOutOfBoundsException exception)
      {
         port = "3306";
      }
      this.pass = Enxada_Homes.getInstance()
          .getConfig()
          .getString("storage.mysql.password");
      this.user = Enxada_Homes.getInstance()
          .getConfig()
          .getString("storage.mysql.username");
      this.url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + database;
      setConnection();
      createTable();
   }

   // Criação de tabela
   public void createTable()
   {
      PreparedStatement stm;
      try
      {
         stm = connection
             .prepareStatement("CREATE TABLE IF NOT EXISTS `enxada_homes` (\n" +
                 "  id VARCHAR(255) NOT NULL DEFAULT (uuid()),\n" +
                 "  player_uuid VARCHAR(255) NOT NULL,\n" +
                 "  last_seen_player_name VARCHAR(45) NOT NULL,\n" +
                 "  last_seen_player_nameLow VARCHAR(45) NOT NULL,\n" +
                 "  player_homes LONGTEXT NOT NULL,\n" +
                 "  PRIMARY KEY (id)\n" +
                 ");");
         stm.executeUpdate();
      } catch (SQLException err)
      {
         Bukkit.getLogger().log(Level.INFO,err.toString());
      }

   }

   // Definindo a conexão
   public void setConnection()
   {
      synchronized ("mysql")
      {
         try
         {
            connection = DriverManager.getConnection(url, user, pass);
         } catch (SQLException err)
         {
            System.out.println("--------------------------------------------------");
            System.out.println("§cNão foi possível se conectar ao banco de dados!");
            System.out.println("--------------------------------------------------");
            err.printStackTrace();
            System.out.println("--------------------------------------------------");
            System.out.println("§cNão foi possível se conectar ao banco de dados!");
            System.out.println("--------------------------------------------------");
         }
      }
   }

   /*
   Verificando se um jogador existe no banco de dados
    */
   public boolean hasPlayer(OfflinePlayer player)
   {
      UUID playerUuid = player.getUniqueId();
      try
      {
         PreparedStatement stm;
         stm = connection
             .prepareStatement("SELECT * FROM `enxada_homes` WHERE `player_uuid` = ?");
         stm.setString(1, playerUuid.toString());

         ResultSet rs = stm.executeQuery();

         return rs.next();
      } catch (SQLException e)
      {
         e.printStackTrace();
         System.out.println("Erro ao salvar homes de " + Bukkit.getOfflinePlayer(playerUuid)
             .getName());
         return false;
      }
   }

   /*
   Criando um jogador no banco de dados
    */
   private void createPlayer(OfflinePlayer player, HomePlayer homePlayer)
   {
      try
      {
         PreparedStatement stm;
         stm = connection
             .prepareStatement("INSERT INTO `enxada_homes` (id, player_uuid, last_seen_player_name, last_seen_player_nameLow, player_homes) VALUES (?, ?, ?, ?, ?);");
         stm.setString(1, UUID.randomUUID()
             .toString());
         stm.setString(2, player.getUniqueId()
             .toString());
         stm.setString(3, player.getName());
         stm.setString(4, player.getName()
             .toLowerCase());
         stm.setString(5, Enxada_Homes.getInstance().jsonHomePlayer.serialize(homePlayer, HomePlayer.class, null)
             .toString());

         stm.executeUpdate();
      } catch (SQLException e)
      {
         e.printStackTrace();
         System.out.println("Erro ao salvar homes de " + player.getName());
      }
   }

   /*
   Atualizar um jogador no banco de dados quando temos todas as informações sobre ele
    */
   public void uploadPlayer(OfflinePlayer player, HomePlayer homePlayer)
   {
      if (hasPlayer(player))
      {
         uploadPlayerData(player, homePlayer);
      }
      else
      {
         createPlayer(player, homePlayer);
      }
   }

   /*
   Atualizar um jogador no banco de dados a partir do nome (busca no cache)
    */
   public void uploadPlayer(String player)
   {
      HomePlayer homePlayer = Enxada_Homes.getInstance().homeAPI.getPlayer(player);
      if (homePlayer != null)
      {
         uploadPlayerData(player, homePlayer);
      }
   }

   /*
   Atualizando as informações de um jogador no banco de dados quando temos
   todas as informações sobre ele.
   Esse método também atualiza o nome do jogador, caso ele tenha mudado de nome.
   Já que o plugin salva por UUID
    */
   private void uploadPlayerData(OfflinePlayer player, HomePlayer homePlayer)
   {
      try
      {
         UUID playerUuid = player.getUniqueId();
         PreparedStatement stm;
         stm = connection
             .prepareStatement("UPDATE `enxada_homes` SET player_homes = ?, last_seen_player_nameLow = ?, last_seen_player_nameLow = ? WHERE `player_uuid` = ?");
         stm.setString(1, Enxada_Homes.getInstance().jsonHomePlayer.serialize(homePlayer, HomePlayer.class, null)
             .toString());
         stm.setString(2, player.getName());
         stm.setString(3, player.getName().toLowerCase());
         stm.setString(4, playerUuid.toString());

         stm.executeUpdate();
      } catch (SQLException e)
      {
         e.printStackTrace();
         System.out.println("Erro ao salvar homes de " + player.getName());
      }
   }

   /*
   Atualizando as informações de um jogador no banco de dados quando temos
   apenas o seu nome e o seu objeto de homes
    */
   private void uploadPlayerData(String player, HomePlayer homePlayer)
   {
      try
      {
         PreparedStatement stm;
         stm = connection
             .prepareStatement("UPDATE `enxada_homes` SET player_homes = ? WHERE `last_seen_player_nameLow` = ?");
         stm.setString(1, Enxada_Homes.getInstance().jsonHomePlayer.serialize(homePlayer, HomePlayer.class, null)
             .toString());
         stm.setString(2, player.toLowerCase());

         stm.executeUpdate();
      } catch (SQLException e)
      {
         e.printStackTrace();
         System.out.println("Erro ao salvar homes de " + player);
      }
   }

   /*
   Buscando as informações de um jogador no banco de dados a partir de seu UUID (recomendado)
    */
   public HomePlayer downloadPlayer(UUID playerUuid)
   {
      try
      {
         PreparedStatement stm;
         stm = connection
             .prepareStatement("SELECT * FROM `enxada_homes` WHERE player_uuid = ?");
         stm.setString(1, playerUuid.toString());

         ResultSet rs = stm.executeQuery();

         if (rs.next())
         {
            String serializedHomes = rs.getString("player_homes");
            JsonElement jsonElement = new JsonParser().parse(serializedHomes);
            return Enxada_Homes.getInstance().jsonHomePlayer.deserialize(jsonElement, HomePlayer.class, null);
         }
      } catch (SQLException e)
      {
         e.printStackTrace();
         System.out.println("Erro ao salvar homes de " + Bukkit.getOfflinePlayer(playerUuid)
             .getName());
      }
      return null;
   }

   /*
   Buscando as informações de um jogador no banco de dados a partir de seu nome
    */
   public HomePlayer downloadPlayer(String name)
   {
      try
      {
         PreparedStatement stm;
         stm = connection
             .prepareStatement("SELECT * FROM `enxada_homes` WHERE last_seen_player_nameLow LIKE ? LIMIT 1;");
         stm.setString(1,name.toLowerCase()+"%");

         Bukkit.getLogger().log(Level.INFO,stm.toString());

         ResultSet rs = stm.executeQuery();

         if (rs.next())
         {
            String serializedHomes = rs.getString("player_homes");
            JsonElement jsonElement = new JsonParser().parse(serializedHomes);
            return Enxada_Homes.getInstance().jsonHomePlayer.deserialize(jsonElement, HomePlayer.class, null);
         }
      } catch (SQLException e)
      {
         e.printStackTrace();
         System.out.println("Erro ao salvar homes de " + name);
      }
      return null;
   }
}
