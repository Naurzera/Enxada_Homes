package com.enxadahost.teste.naurzera.enxada_homes.utils;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import com.enxadahost.teste.naurzera.enxada_homes.objects.EnxadaHome;
import com.enxadahost.teste.naurzera.enxada_homes.objects.HomePlayer;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonHomePlayer
   implements
    JsonSerializer<HomePlayer>,
    JsonDeserializer<HomePlayer>
{
   JsonEnxadaHome jsonEnxadaHome;
   public JsonHomePlayer()
   {
      this.jsonEnxadaHome = Enxada_Homes.getInstance().jsonEnxadaHome;
   }

   /*
   Transformar o objeto HomePlayer em json
    */
   @Override
   public JsonElement serialize(HomePlayer homePlayer, Type typeOfSrc, JsonSerializationContext context)
   {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("playerName", homePlayer.playerName);
      jsonObject.addProperty("playerUuid", homePlayer.playerUuid.toString());
      for (EnxadaHome home : homePlayer.playerHomes)
      {
         jsonObject.add("playerHome_"+home.homeName ,jsonEnxadaHome.serialize(home, EnxadaHome.class, null));
      }

      return jsonObject;
   }

   /*
   Transformar o json do objeto em um Objeto HomePlayer
    */
   @Override
   public HomePlayer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
       JsonParseException
   {
      JsonObject jsonObject = json.getAsJsonObject();
      String playerName = jsonObject.get("playerName")
          .getAsString();
      UUID playerUuid = UUID.fromString(jsonObject.get("playerUuid")
          .getAsString());
      List<EnxadaHome> playerHomes = new ArrayList<>();
      for (Map.Entry<String, JsonElement> element : jsonObject.entrySet())
      {
         if (element.getKey().startsWith("playerHome_"))
         {
            playerHomes.add(jsonEnxadaHome.deserialize(element.getValue(), EnxadaHome.class, null));
         }
      }

      return new HomePlayer(playerUuid,playerName,playerHomes);
   }
}
