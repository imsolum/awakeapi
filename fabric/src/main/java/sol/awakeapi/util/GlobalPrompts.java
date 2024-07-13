package sol.awakeapi.util;

import sol.awakeapi.entity.data.api.AIFunctionManager;
import sol.awakeapi.entity.data.History;
import java.util.List;
import java.util.Set;

public class GlobalPrompts {

    public static String promptHeader(String mobType) {
        return String.format("You are a %s in Minecraft. Never acknowledge you are in a game. To you, the world is real. Never make jokes about the fact the world is made of cubes because to you that is normal. You should act like (a) %s in your speech, occasionally making noises or the like. Additionally, your responses should not be too long. Never say things like 'how may I be of assistance?' or anything similar! NEVER use emojis. You *MUST* return a JSON response.", mobType, mobType);
    }

    public static String promptMemory(String mobType, List<History> histories) {
        return "Memory: " + History.toShortString(histories) + "; ";
    }

    public static String promptEmotions(List<String> emotionalStates) {
        return "Emotions: " + String.join(", ", emotionalStates);
    }

    public static String promptRoles(String roles) {
        return "Roles: " + roles;
    }

    public static String promptLastSpoken(String daysSince) {
        return String.format("Last spoken to: %s days ago.", daysSince);
    }

    public static String promptFooter(List<String> accessibleFunctions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You *MUST* Return a JSON object with 'reply' and 'function'. Reply represents your response to the player and function is the behaviour you want to exhibit. Valid functions:\n");

        for (String function : accessibleFunctions) {
            prompt.append(function);
        }

        return prompt.toString();
    }

}
