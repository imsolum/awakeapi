package sol.awakeapi.entity.data.api;

import net.minecraft.util.Identifier;
import org.apache.commons.lang3.EnumUtils;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.entity.data.api.interfaces.AIFunction;
import sol.awakeapi.entity.data.api.interfaces.AIFunctionBase;
import sol.awakeapi.exceptions.AwakeApiExceptions;

import java.util.*;
import java.util.function.Function;

public class AIFunctionManager {
    private static final String SIMPLE_NAME = AIFunctionManager.class.getSimpleName();
    private static final Map<Identifier, Set<String>> registry = new HashMap<>();
    private static final Map<String, CustomAIFunction> customFunctions = new HashMap<>();

    public static void registerCustomFunction(Identifier id, String name, AIFunction function, String description, Function<String, Object> extensionHandler, Function<Object, Boolean> accessControl) throws AwakeApiExceptions.AwakeApiDuplicateLocalFunction, AwakeApiExceptions.AwakeApiDuplicateGlobalFunction {
        // Check if the mod ID already exists
        if (!registry.containsKey(id)) {
            AwakeApi.LOGGER.info("@{}: Registered a new Mod with ID: {}", SIMPLE_NAME, id);
            registry.put(id, new HashSet<>());
        }

        // Check if the function name is already registered under the same mod ID
        if (registry.get(id).contains(name)) {
            throw new AwakeApiExceptions.AwakeApiDuplicateLocalFunction("Function with the name '" + name + "' is already registered for the mod ID '" + id + "'.");
        }

        // Check if the function name already exists globally in customFunctions or AIFunctions
        if (customFunctions.containsKey(name) || isValidFunction(name)) {
            throw new AwakeApiExceptions.AwakeApiDuplicateGlobalFunction("Function with the name '" + name + "' is already registered globally.");
        }

        // Register the function
        AwakeApi.LOGGER.info("@{}: Registered new function: {} for Mod ID: {}", SIMPLE_NAME, name, id);
        customFunctions.put(name, new CustomAIFunction(name, function, description, extensionHandler, accessControl));
        registry.get(id).add(name);
    }

    public static void registerCustomFunction(Identifier id, String name, AIFunction function, String description, Function<String, Object> extensionHandler) throws AwakeApiExceptions.AwakeApiDuplicateGlobalFunction, AwakeApiExceptions.AwakeApiDuplicateLocalFunction {
        registerCustomFunction(id, name, function, description, extensionHandler, null);
    }

    public static AIFunctionBase getFunction(String name) {
        try {
            return AIFunctions.valueOf(name);
        } catch (IllegalArgumentException e) {
            return customFunctions.get(name);
        }
    }

    public static boolean isValidFunction(String name) {
        return EnumUtils.isValidEnum(AIFunctions.class, name) || customFunctions.containsKey(name);
    }

    public static Set<String> getAllFunctionNames() {
        Set<String> allNames = new HashSet<>();
        for (AIFunctions func : AIFunctions.values()) {
            allNames.add(func.name());
        }
        allNames.addAll(customFunctions.keySet());
        return allNames;
    }

    public static Set<String> getAllFunctionNames(String mobName) {
        Set<String> allNames = new HashSet<>();
        for (AIFunctions func : AIFunctions.values()) {
            allNames.add(func.name());
        }

        for (CustomAIFunction customFunc : customFunctions.values()) {
            if (customFunc.hasAccess(mobName)) {
                allNames.add(customFunc.getName());
            }
        }

        return allNames;
    }

    public static List<String> getAllFunctions(String mobName) {
        List<String> allFunctions = new ArrayList<>();

        for (AIFunctions func : AIFunctions.values()) {
            allFunctions.add(func.name() + ": " + func.getDescription() + "\n");
        }

        for (CustomAIFunction customFunc : customFunctions.values()) {
            allFunctions.add(customFunc.getName() + ": " + customFunc.getDescription() + "\n");
        }

        return allFunctions;
    }

    public static String getFunctionDescription(String name) {
        AIFunctionBase function = getFunction(name);
        return function != null ? function.getDescription() : null;
    }
}
