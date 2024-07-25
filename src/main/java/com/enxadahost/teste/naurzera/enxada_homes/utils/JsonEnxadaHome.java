package com.enxadahost.teste.naurzera.enxada_homes.utils;

import com.enxadahost.teste.naurzera.enxada_homes.Enxada_Homes;
import com.enxadahost.teste.naurzera.enxada_homes.objects.EnxadaHome;
import com.google.gson.*;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.util.UUID;

public class JsonEnxadaHome
   implements
    JsonSerializer<EnxadaHome>,
    JsonDeserializer<EnxadaHome>
{
   /*
   Transformar o objeto EnxadaHome em json
    */
   @Override
   public JsonElement serialize(EnxadaHome src, Type typeOfSrc, JsonSerializationContext context)
   {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("homeName", src.homeName);
      jsonObject.addProperty("ownerName", src.ownerName);
      jsonObject.addProperty("ownerUuid", src.ownerUuid.toString());
      jsonObject.addProperty("isPublic", src.isPublic);
      jsonObject.addProperty("location", Enxada_Homes.getInstance().locationUtils.locationToString(src.location));
      return jsonObject;
   }

   /*
   Transformar o json do objeto em um Objeto EnxadaHome
    */
   @Override
   public EnxadaHome deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
       JsonParseException
   {
      JsonObject jsonObject = json.getAsJsonObject();
      String homeName = jsonObject.get("homeName").getAsString();
      UUID ownerUuid = UUID.fromString(jsonObject.get("ownerUuid").getAsString());
      String ownerName = jsonObject.get("ownerName").getAsString();
      boolean isPublic = jsonObject.get("isPublic").getAsBoolean();
      Location location = Enxada_Homes.getInstance().locationUtils.stringToLocation(jsonObject.get("location").getAsString());
      return new EnxadaHome(homeName,ownerUuid,ownerName,isPublic,location);
   }
}
