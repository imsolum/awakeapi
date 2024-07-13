package sol.awakeapi.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.command.type.ModelArgumentType;
import sol.awakeapi.networking.AwakeApiPackets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SetAI {

    public static final String SIMPLE_NAME = SetAI.class.getSimpleName();
    public static final String defaultModel = "gpt-4o";
    public static final String openAIEndpoint = "https://api.openai.com/v1/chat/completions";

    public static void register(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        serverCommandSourceCommandDispatcher.register(CommandManager.literal("setAI")
                .then(CommandManager.argument("API key", StringArgumentType.string())
                        .then(CommandManager.argument("Model", ModelArgumentType.model())
                                .executes(context -> runBase(context,
                                        StringArgumentType.getString(context, "API key"),
                                        ModelArgumentType.getModel(context, "Model")))
                        )
                        .then(CommandManager.argument("Model", ModelArgumentType.model())
                                .then(CommandManager.argument("Endpoint", StringArgumentType.greedyString())
                                        .executes(context -> runCustom(context,
                                                StringArgumentType.getString(context, "API key"),
                                                ModelArgumentType.getModel(context, "Model"),
                                                StringArgumentType.getString(context, "Endpoint"))))
                        ))
                .then(CommandManager.argument("Endpoint", StringArgumentType.greedyString())
                        .executes(context -> runOobabooga(context, StringArgumentType.getString(context, "Endpoint"))))
        );
    }

    private static int runOobabooga(CommandContext<ServerCommandSource> context, String endpoint) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        assert player != null;

        AtomicBoolean succeeded = new AtomicBoolean(false);
        context.getSource().sendFeedback(() -> Text.literal("Processing..."), false);
        getAvailableModelsAsync("dummy", getBaseEndpoint(endpoint), context.getSource()).thenAccept(r -> {
            if (!r.isEmpty()) {
                NbtCompound nbt = new NbtCompound();
                nbt.putString("type", "ooba");
                nbt.putString("endpoint", endpoint);

                ServerPlayNetworking.send(player, AwakeApiPackets.CLIENTBOUND_UPDATE_AI_PARAMS, PacketByteBufs.create().writeNbt(nbt));

                context.getSource().sendFeedback(() -> Text.literal("Set successfully!"), false);
            } else {
                context.getSource().sendFeedback(() -> Text.literal("Could not confirm endpoint is running. Please check."), false);
            }
        });
        return succeeded.get() ? 1 : 0;
    }

    private static int runCustom(CommandContext<ServerCommandSource> context, String key, String modelName, String endpoint) {
        AtomicBoolean succeeded = new AtomicBoolean(false);
        context.getSource().sendFeedback(() -> Text.literal("Processing..."), false);
        getAvailableModelsAsync(key, getBaseEndpoint(endpoint), context.getSource()).thenAccept(availableModels -> {
            if (!availableModels.isEmpty()) {
                if (availableModels.contains(modelName)) {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    assert player != null;

                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("type", "custom");
                    nbt.putString("key", key);
                    nbt.putString("model", modelName);
                    nbt.putString("endpoint", endpoint);

                    ServerPlayNetworking.send(player, AwakeApiPackets.CLIENTBOUND_UPDATE_AI_PARAMS, PacketByteBufs.create().writeNbt(nbt));

                    context.getSource().sendFeedback(() -> Text.literal("Key and model set successfully on custom endpoint!"), false);
                    succeeded.set(true);
                } else {
                    if (!modelName.equalsIgnoreCase("model")) {
                        context.getSource().sendFeedback(() -> Text.literal("The model you requested is not available at the endpoint you selected. To view all (?) models at this endpoint, re-run the command, passing in the literal `model` as the model."), false);
                    } else {
                        String accessibleModels = String.join(", ", availableModels);
                        context.getSource().sendFeedback(() -> Text.literal(accessibleModels), false);
                        context.getSource().sendFeedback(() -> Text.literal("It's possible there are too many models to fit in the in-game chat. If that is the case, please refer to your endpoint's documentation to determine available models"), false);
                    }
                }
            } else {
                context.getSource().sendFeedback(() -> Text.literal("There are no models available at the endpoint you requested. Please check your API key / endpoint. A typical endpoint ends in `/completions` (local endpoints would likely differ)."), false);
            }
        });
        return succeeded.get() ? 1 : 0;
    }

    public static int runBase(CommandContext<ServerCommandSource> context, String key, String modelName) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        modelName = modelName.toLowerCase();

        // Check if the model name contains "GPT"
        if (!modelName.contains("gpt")) {
            context.getSource().sendFeedback(() -> Text.literal("Please choose a valid GPT model (i.e. models with 'gpt' in their name)."), false);
            return 0;
        }

        // Notify the user that the system is processing the request
        context.getSource().sendFeedback(() -> Text.literal("Processing..."), false);

        // Verify key and model asynchronously
        String finalModelName = modelName;
        getAvailableModelsAsync(key, getBaseEndpoint(openAIEndpoint), context.getSource()).thenAccept(availableModels -> {
            if (!availableModels.isEmpty()) {
                if (!availableModels.contains(finalModelName)) {
                    context.getSource().sendFeedback(() -> Text.literal("The provided model is not accessible with the given key."), false);

                    // Send the models the user does have access to
                    String accessibleModels = String.join(", ", availableModels);
                    context.getSource().sendFeedback(() -> Text.literal("Accessible models with the given key are: " + accessibleModels), false);
                } else {
                    assert player != null;

                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("type", "base");
                    nbt.putString("key", key);
                    nbt.putString("model", finalModelName);
                    nbt.putString("endpoint", openAIEndpoint);

                    ServerPlayNetworking.send(player, AwakeApiPackets.CLIENTBOUND_UPDATE_AI_PARAMS, PacketByteBufs.create().writeNbt(nbt));

                    // Provide feedback to the player
                    context.getSource().sendFeedback(() -> Text.literal("OpenAI Key and model set successfully!"), false);
                }
            } else {
                context.getSource().sendFeedback(() -> Text.literal("Provided key invalid. (No models with that key determined)"), false);
            }
        });
        return 1;
    }

    public static CompletableFuture<List<String>> getAvailableModelsAsync(String apiKey, String baseEndpoint, ServerCommandSource source) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> models = new ArrayList<>();

            try {
                String modelsEndpoint = baseEndpoint + "/v1/models";
                URL url = new URL(modelsEndpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);

                int responseCode = connection.getResponseCode();
                AwakeApi.LOGGER.debug("@{}: Response Code: {}", SIMPLE_NAME, responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String response = in.lines().collect(Collectors.joining());
                        AwakeApi.LOGGER.debug("@{}: Response: {}", SIMPLE_NAME, response);

                        JsonElement responseElement = JsonParser.parseString(response);
                        if (responseElement.isJsonObject()) {
                            JsonObject responseObject = responseElement.getAsJsonObject();
                            if (responseObject.has("data") && responseObject.get("data").isJsonArray()) {
                                JsonArray dataArray = responseObject.getAsJsonArray("data");
                                for (JsonElement element : dataArray) {
                                    JsonObject modelObject = element.getAsJsonObject();
                                    String modelName = modelObject.get("id").getAsString();
                                    models.add(modelName);
                                }
                            } else {
                                AwakeApi.LOGGER.error("@{}: ERROR: Response JSON object does not contain 'data' array", SIMPLE_NAME);
                                source.sendFeedback(() -> Text.literal("The endpoint you requested returned an unexpected JSON object. Please refer to your endpoint's documentation."), false);
                            }
                        } else if (responseElement.isJsonArray()) {
                            JsonArray dataArray = responseElement.getAsJsonArray();
                            for (JsonElement element : dataArray) {
                                JsonObject modelObject = element.getAsJsonObject();
                                String modelName = modelObject.get("id").getAsString();
                                models.add(modelName);
                            }
                        } else {
                            AwakeApi.LOGGER.error("@{}: ERROR: Response is neither a JSON array nor a JSON object", SIMPLE_NAME);
                            source.sendFeedback(() -> Text.literal("The endpoint you requested returned an unexpected JSON response. Please refer to your endpoint's documentation."), false);
                        }
                    }
                } else {
                    AwakeApi.LOGGER.error("@{}: ERROR: Response Code: {}", SIMPLE_NAME, responseCode);
                    source.sendFeedback(() -> Text.literal("The endpoint you requested returned an error code of " + responseCode + ". While this could imply the endpoint is valid, please refer to your endpoint's documentation to interpret this error code."), false);
                }
            } catch (Exception e) {
                AwakeApi.LOGGER.error("@{}: ERROR: {}", SIMPLE_NAME, e);
            }
            return models;
        });
    }

    public static String getBaseEndpoint(String endpoint) {
        int count = 0;
        int position = 0;

        for (int i = 0; i < endpoint.length(); i++) {
            if (endpoint.charAt(i) == '/') {
                count++;
                if (count == 3) {
                    position = i;
                    break;
                }
            }
        }

        // If the third slash was found, substring up to its position.
        // If not, return the original string.
        return (count >= 3) ? endpoint.substring(0, position) : endpoint;
    }
}
