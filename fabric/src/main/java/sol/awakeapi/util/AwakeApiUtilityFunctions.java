package sol.awakeapi.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;

import java.util.UUID;

public class AwakeApiUtilityFunctions {

    public static String extractMessageFromResponse(String jsonResponse) {
        if (jsonResponse != null) {
            JsonObject responseObj = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Check if there's a detail field indicating an error
            if (responseObj.has("detail")) {
                return responseObj.get("detail").getAsString();
            }

            // Check for the choices array
            if (responseObj.has("choices")) {
                JsonArray choices = responseObj.getAsJsonArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    if (firstChoice.has("message")) {
                        JsonObject message = firstChoice.getAsJsonObject("message");
                        if (message != null && message.has("content")) {
                            return message.get("content").getAsString();
                        }
                    }
                }
                return "No valid message found in choices. Displaying potential error: " + jsonResponse;
            }

            return "Unexpected response structure. Displaying response: " + jsonResponse;
        } else {
            return null;
        }
    }

    public static MobEntity getEntityByUUID(UUID uuid, MinecraftClient mc) {
        if (mc.world != null) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity.getUuid().equals(uuid)) {
                    if (entity instanceof MobEntity mob) {
                        return mob;
                    }
                }
            }
        }
        return null;
    }
}
