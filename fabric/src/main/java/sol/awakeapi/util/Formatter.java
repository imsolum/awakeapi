package sol.awakeapi.util;

import com.google.gson.*;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.Nullable;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.api.api_data.AIData;
import sol.awakeapi.api.api_data.AIParams;
import sol.awakeapi.api.api_data.Message;
import sol.awakeapi.entity.data.api.AIFunctions;
import java.util.ArrayList;
import java.util.List;

public class Formatter {

    private final static String SIMPLE_NAME = java.util.Formatter.class.getSimpleName();

    private static JsonObject getDefaultJsonObject (AIParams params) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", params != null ? params.getModel() : "model (called server-side)");
        requestBody.addProperty("temperature", 1.0);
        requestBody.addProperty("max_tokens", 256);
        requestBody.addProperty("top_p", 1.0);
        requestBody.addProperty("frequency_penalty", 0.0);
        requestBody.addProperty("presence_penalty", 0.0);
        return requestBody;
    }

    public static JsonObject formatCustomMessagesForAI(AIParams params, String systemMessage, boolean isImage) {
        JsonObject requestBody = getDefaultJsonObject(params);
        JsonArray promptMessages = new JsonArray();

        if (isImage) {
            promptMessages.add(createImageMessage(systemMessage));
        }
        else {
            promptMessages.add(createSystemMessage(systemMessage));
        }

        requestBody.add("messages", promptMessages);

        return requestBody;
    }

    public static JsonObject formatCustomMessagesForAI(AIParams params, List<String> systemMessage) {
        JsonObject requestBody = getDefaultJsonObject(params);
        JsonArray promptMessages = new JsonArray();

        for (String message : systemMessage) {
            promptMessages.add(createSystemMessage(message));
        }

        requestBody.add("messages", promptMessages);

        return requestBody;
    }

    public static JsonObject formatMessagesForAI(AIData data, AIParams params, String mobName, long currentTimestamp) {
        JsonObject requestBody = getDefaultJsonObject(params);

        float daysSince = daysSinceLastMessage(data.messages(), currentTimestamp);

        JsonArray promptMessages = getMessages(data, mobName, String.format("%.1f", daysSince));

        requestBody.add("messages", promptMessages);

        return requestBody;
    }

    private static JsonArray getMessages(AIData data, String mobName, String daysSince) {
        JsonArray promptMessages = new JsonArray();

        promptMessages.add(createSystemMessage(GlobalPrompts.promptHeader(mobName)));

        for (Message msg : data.messages()) {
            JsonObject formattedMessage = new JsonObject();
            formattedMessage.addProperty("role", msg.isUser() ? "user" : "assistant");
            formattedMessage.addProperty("content", msg.content());
            promptMessages.add(formattedMessage);
        }

        promptMessages.add(createSystemMessage(GlobalPrompts.promptMemory(mobName, data.histories())));
        promptMessages.add(createSystemMessage(GlobalPrompts.promptEmotions(data.emotions())));

        if (!data.roles().isEmpty()) {
            promptMessages.add(createSystemMessage(GlobalPrompts.promptRoles(data.roles())));
        }

        if (!daysSince.equals("0.0")) {
            promptMessages.add(createSystemMessage(GlobalPrompts.promptLastSpoken(daysSince)));
        }

        promptMessages.add(createSystemMessage(GlobalPrompts.promptFooter(data.accessibleFunctions())));

        if (data.systemMessage() != null) {
            if (data.isImage() != null && data.isImage()) {
                promptMessages.add(createImageMessage(data.systemMessage()));
            } else {
                promptMessages.add(createSystemMessage(data.systemMessage()));
            }
        }

        return promptMessages;
    }

    private static JsonObject createSystemMessage(String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "system");
        message.addProperty("content", content);
        return message;
    }

    private static JsonObject createUserMessage(String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", content);
        return message;
    }


    private static JsonObject createImageMessage(String base64Image) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");

        JsonArray contentArray = new JsonArray();

        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", "The following is a screenshot taken from your point of view in game. You may describe what you see to the player.");
        contentArray.add(textContent);

        JsonObject imageContent = new JsonObject();
        imageContent.addProperty("type", "image_url");
        JsonObject imageUrl = new JsonObject();
        imageUrl.addProperty("url", "data:image/jpeg;base64," + base64Image);
        imageContent.add("image_url", imageUrl);
        contentArray.add(imageContent);

        message.add("content", contentArray);
        return message;
    }

    private static float daysSinceLastMessage(List<Message> messages, long currentTimestamp) {
        if (messages.size() < 2) {
            return 0;
        }

        long lastMessageTimestamp = messages.get(messages.size() - 2).timestamp();

        // Calculate the difference in ticks
        long tickDifference = currentTimestamp - lastMessageTimestamp;

        // Convert ticks to in-game days
        return tickDifference / 24000f; // One in-game day is 24,000 ticks
    }

    public static String getValidReplyFromJson(String response) {
        try {
            return validateResponse(response, null)[0];
        } catch (Exception e) {
            AwakeApi.LOGGER.warn("@{}: Failed to format message. Exception details: {}", SIMPLE_NAME, e);
            return response;
        }
    }

    public static List<String> extractFunctionNames(List<String> inputList) {
        List<String> result = new ArrayList<>();
        for (String entry : inputList) {
            // Split each entry at the colon and take the first part
            String firstPart = entry.split(":")[0].trim();
            result.add(firstPart);
        }
        return result;
    }

    public static String[] validateResponse(String response, @Nullable List<String> availableFunctions) throws Exception {
        if (availableFunctions != null) {
            availableFunctions = extractFunctionNames(availableFunctions);
        }

        // Strip to the content between the first and last curly braces
        int firstBrace = response.indexOf('{');
        int lastBrace = response.lastIndexOf('}');

        if (firstBrace == -1 || lastBrace == -1 || firstBrace > lastBrace) {
            throw new Exception("Invalid JSON response format: " + response);
        }

        String jsonResponse = response.substring(firstBrace, lastBrace + 1);

        // Parse the JSON
        JsonElement jsonElement;
        try {
            jsonElement = JsonParser.parseString(jsonResponse);
        } catch (JsonSyntaxException e) {
            throw new Exception("Invalid JSON syntax: " + response);
        }

        if (!jsonElement.isJsonObject()) {
            throw new Exception("Response is not a JSON object: " + response);
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        // Validate the fields
        if (!jsonObject.has("reply") || !jsonObject.has("function")) {
            throw new Exception("Missing required fields: reply or function: " + response);
        }

        String reply = jsonObject.get("reply").getAsString().trim();
        String function = jsonObject.get("function").getAsString().trim();

        // Remove quotation marks if they exist
        if (reply.startsWith("\"") && reply.endsWith("\"")) {
            reply = reply.substring(1, reply.length() - 1);
        }

        if (function.startsWith("\"") && function.endsWith("\"")) {
            function = function.substring(1, function.length() - 1);
        }

        // Validate the function field
        String rawFunction = function;
        String functionParam = "";
        int functionExtension = function.indexOf("_");
        if (functionExtension > 0) {
            rawFunction = function.substring(0, functionExtension);
            functionParam = function.substring(functionExtension + 1);
        }

        // Check if availableFunctions is provided and not empty
        if (availableFunctions == null || availableFunctions.isEmpty()) {
            // Validate against the AIFunctions enum
            if (!EnumUtils.isValidEnum(AIFunctions.class, rawFunction)) {
                throw new Exception("Invalid function value: " + response);
            }
        } else {
            // Validate against the provided availableFunctions list
            if (!availableFunctions.contains(rawFunction.toUpperCase()) && !EnumUtils.isValidEnum(AIFunctions.class, rawFunction)) {
                throw new Exception("Invalid function value: " + response);
            }
        }

        return new String[]{reply, rawFunction, functionParam};
    }
}
