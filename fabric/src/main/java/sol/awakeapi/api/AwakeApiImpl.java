package sol.awakeapi.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.api.api_data.AIData;
import sol.awakeapi.api.api_data.AIParams;
import sol.awakeapi.api.api_data.Message;
import sol.awakeapi.api.interfaces.AIQueryHandler;
import sol.awakeapi.interfaces.IInGameHud;
import sol.awakeapi.interfaces.IPlayerEntity;
import sol.awakeapi.networking.AwakeApiPackets;
import sol.awakeapi.util.AwakeApiAsyncRequest;
import sol.awakeapi.util.AwakeApiUtilityFunctions;
import sol.awakeapi.util.Formatter;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class provides a large number of useful methods
 * to make use of the AI model set by the user.
 * <p>For other API interfaces, see also:</p>
 * @see AwakeApiImpl
 * @see AwakeApiQueryReg
 */
public class AwakeApiImpl {
    private static final String SIMPLE_NAME = AwakeApiImpl.class.getSimpleName();


    /**
     * Server-side accessor to get a response from the AI
     * When called, it will grab any data from the server
     * player and send it to who they're currently speaking to
     * before saving the AI's response as a new message
     * <br></br><br></br>
     * In usage, would be called after storing a message object
     * on the player. Generally, wouldn't need to be called by
     * developers, but if desired, see the following implementation:
     * <br><br><pre>{@code
     * ServerPlayerEntity sender = ...;
     * String messageContent = ...;
     * boolean isUser = true;
     * MobEntity speakingTo = AwakeApiImpl.getSpeakingTo(sender);
     * if (speakingTo != null) {
     *     long currentTime = sender.getWorld().getTimeOfDay();
     *     Message message = new Message(speakingTo.getUuid(), currentTime, messageContent, isUser);
     *     AwakeApiImpl.addMessage(sender, message);
     *     AwakeApiImpl.getResponse(sender);
     * }
     * }</pre>
     * @param player
     * The ServerPlayerEntity that will be the point of reference
     * for any code execution.
     * @see Message
     */
    public static void getResponse(ServerPlayerEntity player) {
        // Get the AIData from the player
        AIData data = ((IPlayerEntity) player).getAIData();
        NbtCompound compound = data.toNbt();

        // Send AIData to the client side
        // Once received, execution will follow from there.
        ServerPlayNetworking.send(player, AwakeApiPackets.CLIENTBOUND_API_GET_RESPONSE, PacketByteBufs.create().writeNbt(compound));
    }

    /**
     * Server-side accessor to get a standard response from the AI
     * while also passing a custom instruction.
     * <p><b>For more detailed documentation, see also<b/></p>
     * @param player The ServerPlayerEntity that will be the point
     * of reference for any code execution.
     * @param systemMessage A system message to pass to the AI,
     * useful if implementing sending a picture to the AI or
     * passing it an instruction for a different feature.
     * @param isImage Boolean to denote if the systemMessage passed is an image (which will flatten it to user)
     * @see #getResponse(ServerPlayerEntity)
     */
    public static void getResponse(ServerPlayerEntity player, String systemMessage, boolean isImage) {
        // Get the AIData from the player
        AIData data = ((IPlayerEntity) player).getAIData(systemMessage, isImage);
        NbtCompound compound = data.toNbt();

        // Send AIData to the client side
        // Once received, execution will follow from there.
        ServerPlayNetworking.send(player, AwakeApiPackets.CLIENTBOUND_API_GET_RESPONSE, PacketByteBufs.create().writeNbt(compound));
    }

    /**
     * Client-side accessor to get a response from the AI
     * @param player
     * The ClientPlayerEntity that will receive the response
     * @param data
     * The AIData pertaining to the AI they are interacting with
     *
     * <p><b>Developers should use the server-side implementation
     * (see also)</b></p>
     * @see #getResponse(ServerPlayerEntity)
     * @see #getResponse(ServerPlayerEntity, String, boolean)
     */
    public static void getResponse(ClientPlayerEntity player, AIData data) {
        // Get the player's AIParams
        AIParams params = ((IPlayerEntity) player).getAIParams();

        if (params.isValid()) {
            // Get the mob's name
            MobEntity mob = AwakeApiUtilityFunctions.getEntityByUUID(data.entityUuid(), MinecraftClient.getInstance());
            String mobName = "";
            if (mob != null) {
                mobName = mob.getType().getName().getString();
            }

            long currentTime = player.clientWorld.getTimeOfDay();

            // Prepare JSON for API request
            JsonObject formattedRequest = Formatter.formatMessagesForAI(data, params, mobName, currentTime);
            Gson gson = new Gson();
            String jsonRequest = gson.toJson(formattedRequest);
            CompletableFuture<String> future = AwakeApiAsyncRequest.sendAsyncRequestToAI(jsonRequest, params);

            // Once a response has been received:
            future.thenAccept(response -> {
                String rawReply = AwakeApiUtilityFunctions.extractMessageFromResponse(response);

                // Pair value used to hold AI's reply (pair[0]), as
                // well as the function it called (pair[1])
                String[] pair = new String[]{};
                boolean clean = false;

                try {
                    // Used to confirm if AI response conforms to structure and called a valid AIFunction
                    pair = Formatter.validateResponse(rawReply, data.accessibleFunctions());
                    clean = true;
                } catch (Exception e) {
                    AwakeApi.LOGGER.warn("@{}: Invalid response retrieved from AI. Details below...", SIMPLE_NAME);
                    AwakeApi.LOGGER.warn("@{}: {}", SIMPLE_NAME, e);

                    JsonObject responseObj = JsonParser.parseString(response).getAsJsonObject();

                    // If the AI response at least has a detail entry, send it to the player
                    if (responseObj.has("detail")) {
                        String text = responseObj.get("detail").getAsString();
                        displayReply(player, text);
                    }
                }

                // If no error was found:
                if (clean) {
                    // Obtain AI's reply
                    String reply = pair[0];

                    // Display the reply to the player
                    displayReply(player, reply);

                    // Create Message object
                    Message msg = new Message(data.entityUuid(), player.clientWorld.getTimeOfDay(), reply, false);

                    // Store message server side
                    ClientPlayNetworking.send(AwakeApiPackets.SERVERBOUND_ADD_MESSAGE, PacketByteBufs.create().writeNbt(msg.toNbt()));

                    // Execute the function (if any) on the server
                    if (mob != null) {
                        // Obtain the function the AI called
                        String function = pair[1];
                        String functionExtension = pair[2];
                        boolean isGroup = false;

                        // Prepare the data
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeUuid(mob.getUuid());
                        buf.writeString(function);
                        buf.writeString(functionExtension);
                        buf.writeBoolean(isGroup);

                        // Send to server
                        ClientPlayNetworking.send(AwakeApiPackets.SERVERBOUND_EXECUTE_AI_FUNCTION, buf);
                    } else {
                        AwakeApi.LOGGER.warn("@{}: Cannot execute function (if any) because the expected mob of UUID: {} could not be found. This can occur, for example, if a response is requested but the mob dies before it can be found / unloaded or if the player themself moves to an area outside the render spec of the mob.", SIMPLE_NAME, data.entityUuid());
                    }
                }
            });
        } else {
            AwakeApi.LOGGER.error("@{}: Could not get a response from the AI. Have you called `/setAI` yet?", SIMPLE_NAME);
            displayReply(player, "Could not get a response from the AI. Have you called `/setAI` yet?");
        }
    }

    /**
     * Server-side accessor to query the AI with custom instructions.
     * This method allows you to request the AI for specific tasks or information
     * outside of standard player-mob conversations.
     *
     * <p>This method sends the query to the client side where the AI processing
     * occurs, and the result is sent back to the server. You must provide a callback
     * function that will handle the AI's response.</p>
     *
     * <p><b>Example usage:</b></p>
     * <pre>{@code
     * ServerPlayerEntity player = ...;
     * String systemMessage = "Explain the significance of quantum computing.";
     * boolean isImage = false;
     *
     * queryAI(player, systemMessage, isImage, response -> {
     *     System.out.println("AI Response: " + response);
     *     // Perform further processing with the response
     * });
     * }</pre>
     *
     * @param player The player entity sending the query
     * @param systemMessage The message or instruction for the AI
     * @param isImage boolean to denote if the systemMessage is
     *                an image (i.e. base64 encoded string)
     * @param callback The function to handle the AI's response
     * @see #queryAI(ServerPlayerEntity, List, AIQueryHandler)
     *
     * @implNote It is <b>very important</b> to keep the system
     * message concise to avoid exceeding the API's input token limits.
     * Large inputs may be truncated or rejected. Additionally, the
     * function you pass as a callback will receive a raw {@code String} response,
     * as such you will need to validate and parse this yourself.
     */
    public static void queryAI(ServerPlayerEntity player, String systemMessage, boolean isImage, AIQueryHandler callback) {
        PacketByteBuf buf = PacketByteBufs.create();

        // Create a callbackID for the callback
        UUID callbackId = AwakeApiQueryReg.registerCallback(callback);

        buf.writeInt(1);
        buf.writeString(systemMessage);
        buf.writeBoolean(isImage);
        buf.writeUuid(callbackId);

        ServerPlayNetworking.send(player, AwakeApiPackets.CLIENTBOUND_QUERY_AI, buf);
    }

    /**
     * <p><b>Example Implementation of a Callback Function with a list of system messages:</b></p>
     * <pre>{@code
     * public class CustomFunctions {
     *     public static void generateQuest(String response) {
     *         if (response == null || response.isEmpty()) {
     *             System.out.println("Received an empty response from the AI.");
     *         } else {
     *             System.out.println("AI Response: " + response);
     *             // Add further validation and processing here
     *             // e.g. validateQuestJson(response)
     *             // if valid...
     *         }
     *     }
     * }
     *
     * // Register the function with the AI query
     * ServerPlayerEntity player = ...;
     * String header = "You are a quest generator for a Minecraft mod. Generate a quest in JSON format with the following...";
     * String example = "The following is an example valid JSON: {\n" +
     *                 "    \"name\": \"Magic Stone\",\n" + ...}"
     * List<String> prompts = new ArrayList();
     * prompts.add(header);
     * prompts.add(example);
     *
     * queryAI(player, prompts, CustomFunctions::generateQuest);
     * }</pre>
     * @param player ServerPlayerEntity that will be the point of
     *               reference for execution
     * @param systemMessage List of {@code String} objects to be
     *                      used as system messages
     * @param callback The function to handle the AI's response
     * <br><br><p>For more detailed documentation:</p>
     */
    public static void queryAI(ServerPlayerEntity player, List<String> systemMessage, AIQueryHandler callback) {
        PacketByteBuf buf = PacketByteBufs.create();

        // Create a callbackID for the callback
        UUID callbackId = AwakeApiQueryReg.registerCallback(callback);

        buf.writeInt(systemMessage.size());
        for (String msg : systemMessage) {
            buf.writeString(msg);
        }

        buf.writeUuid(callbackId);

        ServerPlayNetworking.send(player, AwakeApiPackets.CLIENTBOUND_QUERY_AI, buf);
    }


    /**
     * Get a response from the AI for usage outside of standard
     * player-mob communication.
     * <p><b>Developers should use the server-side implementation</b></p>
     * @param player
     * @param systemMessage
     * @param isImage
     *
     * @see #queryAI(ServerPlayerEntity, String, boolean, AIQueryHandler)
     */
    public static void queryAI(ClientPlayerEntity player, String systemMessage, boolean isImage, UUID callbackId) {
        AIParams params = ((IPlayerEntity) player).getAIParams();

        if (params.isValid()) {
            JsonObject formattedRequest = Formatter.formatCustomMessagesForAI(params, systemMessage, isImage);
            Gson gson = new Gson();
            String jsonRequest = gson.toJson(formattedRequest);

            CompletableFuture<String> future = AwakeApiAsyncRequest.sendAsyncRequestToAI(jsonRequest, params);

            future.thenAccept(response -> {
                String rawAIReply = AwakeApiUtilityFunctions.extractMessageFromResponse(response);

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString(rawAIReply);
                buf.writeUuid(callbackId);

                ClientPlayNetworking.send(AwakeApiPackets.SERVERBOUND_QUERY_AI, buf);
            });
        }  else {
            AwakeApi.LOGGER.error("@{}: Could not get a response from the AI. Have you called `/setAI` yet?", SIMPLE_NAME);
            displayReply(player, "Could not get a response from the AI. Have you called `/setAI` yet?");
        }
    }

    /**
     * Get a response from the AI for usage outside of standard
     * player-mob communication.
     * <p><b>Developers should use the server-side implementation</b></p>
     * @param player
     * @param systemMessages
     *
     * @see #queryAI(ServerPlayerEntity, String, boolean, AIQueryHandler)
     */
    public static void queryAI(ClientPlayerEntity player, List<String> systemMessages, UUID callbackId) {
        AIParams params = ((IPlayerEntity) player).getAIParams();

        if (params.isValid()) {
            JsonObject formattedRequest = Formatter.formatCustomMessagesForAI(params, systemMessages);
            Gson gson = new Gson();
            String jsonRequest = gson.toJson(formattedRequest);

            CompletableFuture<String> future = AwakeApiAsyncRequest.sendAsyncRequestToAI(jsonRequest, params);

            future.thenAccept(response -> {
                String rawAIReply = AwakeApiUtilityFunctions.extractMessageFromResponse(response);

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString(rawAIReply);
                buf.writeUuid(callbackId);

                ClientPlayNetworking.send(AwakeApiPackets.SERVERBOUND_QUERY_AI, buf);
            });
        }  else {
            AwakeApi.LOGGER.error("@{}: Could not get a response from the AI. Have you called `/setAI` yet?", SIMPLE_NAME);
            displayReply(player, "Could not get a response from the AI. Have you called `/setAI` yet?");
        }
    }

    /**
     * Displays a message in the player's HUD directly above their
     * inventory
     * @implNote Possibly effected by user's in-game resolution.
     * Additionally, can override other content being displayed
     * at the same location (i.e. other display replies).
     * @param ignoredClientPlayer
     * The ClientPlayerEntity that will display the message
     * @param message
     * The message to display
     * @param duration
     * Integer representing duration in ticks (180 recommended)
     * @see #displayReply(ClientPlayerEntity, String)
     */
    public static void displayReply(ClientPlayerEntity ignoredClientPlayer, String message, int duration) {
        MinecraftClient client = MinecraftClient.getInstance();
        ((IInGameHud) client.inGameHud).displayOverlayMessage(message, duration);
    }

    /**
     * Displays a message in the player's HUD directly above their
     * inventory
     * @implNote Possibly effected by user's in-game resolution.
     * Additionally, can override other content being displayed
     * at the same location.
     * @param ignoredClientPlayer
     * The ClientPlayerEntity that will display the message
     * @param message
     * The message to display
     * @see #displayReply(ClientPlayerEntity, String, int)
     */
    public static void displayReply(ClientPlayerEntity ignoredClientPlayer, String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        ((IInGameHud) client.inGameHud).displayOverlayMessage(message, 180);
    }
}
