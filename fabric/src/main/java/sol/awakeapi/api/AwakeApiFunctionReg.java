package sol.awakeapi.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sol.awakeapi.entity.data.api.AIFunctionManager;
import sol.awakeapi.entity.data.api.interfaces.AIFunction;
import sol.awakeapi.exceptions.AwakeApiExceptions;
import java.util.function.Function;

/**
 * <p>An API for registering custom functions.</p>
 * <p>The following are optional extensions that
 * are referred to in this class and their purposes:</p>
 * <ol>
 *     <li>{@code extensionHandler}. A utility function
 *     to convert a {@code String} into an {@code Object}.
 *     This method can be used to provide extra utility
 *     to the AI's function calls. Typically, this would
 *     only be needed if the function being registered
 *     requires more detail than can be objectively
 *     written / cannot be determined at compile time.<br>
 *     <br><p>This {@code Object} would then be passed to
 *     the main method that is executed by the function
 *     in the form of {@code AIFunctionParams.extension()}</p>
 *     <br><p>In the case of {@code extensionHandler}, it would
 *     probably be useful to convert the {@code String} into
 *     a class such as {@code Class<? extends MobEntity>}
 *     or a {@code RegistryKey<?>}.</p>
 *     <br><b>The documentation for usage examples is detailed,
 *     so refer to them for in-game examples</b></li>
 *     <li>{@code accessControl}. A utility function
 *     to convert a {@code String} into a {@code boolean}.
 *     This would allow the function to specify
 *     what mobs should have access to the function.<br>
 *     <br><p>The string passed to {@code accessControl}
 *     will be the <b>name</b> of the mob type. For
 *     example, 'zombie', or 'iron_golem'. which can
 *     then be validated.</p></li>
 * </ol>
 */
public class AwakeApiFunctionReg {

    /**
     * Register a single custom AI function.
     * <br>
     * <br>
     * Expected structure:
     * <pre>{@code
     * AwakeApiFunctionReg.registerFunction(
     *     "{NAME}",
     *     YourBehaviourImplClass::someMethod,
     *     "Method description. Usage: {NAME}",
     *     (extension) -> YourExtensionHandlerClass::handleExtension
     * );
     * }</pre>
     * <p>Example usage:</p>
     * <pre>{@code
     * AwakeApiFunctionReg.registerFunction(
     *      "FOLLOW",
     *      AIBehaviours::follow,
     *      "Follow the player. Usage: FOLLOW",
     *      null
     * );
     *
     * AwakeApiFunctionReg.registerFunction(
     *      "ATTACK",
     *      CustomModBehaviours::customAttack,
     *      "Attack a specific mob. Usage: ATTACK_{MOB_NAME}",
     *      (extension) -> CustomMobMaps.getCustomMobClass(extension)
     * );
     * }</pre>
     * @implNote Ideally, the name of your function should be
     * literally what it does. It'd probably be more beneficial
     * if it is also titled something the player might say, as
     * generally the AI is influenced by the player's speech.
     * Of course, this can also mean the function title has
     * the chance of backfiring if it is something too trivial, like
     * {@code WALK} or {@code EAT} since the AI might try to repeatedly
     * call them.
     * <br><p>It is also possible to allow the AI to append specific instructions
     * to their function call. Consider the following example:</p>
     * <pre>{@code
     * AwakeApiFunctionReg.registerFunction(
     *     "ATTACK",
     *     AIBehaviours::attack,
     *     "Attack a specified mob or nearest hostile enemy. Usage: ATTACK_{MOB_NAME}",
     *     (extension) -> CustomMobMaps.getCustomMobClass(extension)
     * );
     * }</pre>
     * <p>In the above example, we're telling the AI model that they can
     * specify a mob to attack, in which case we can expect the AI
     * might return something like {@code ATTACK_ZOMBIE}. In this
     * instance, we can define how to handle the extension using the
     * provided {@code extensionHandler}. Your {@code extensionHandler}
     * would thus be a method that transforms a String (whatever the AI
     * wrote), into an Object that would be later useful to you within
     * the {@code AIFunctionParams}. NOTE: You would almost certainly have to
     * validate the {@code AIFunctionParams.extension}. <br>To validate it,
     * you could easily check if it is an instanceof whatever you need it
     * to be.</p>
     *
     * <br><p>It is also possible to specify what mobs can access certain commands.
     * For more detail on this, see also.</p>
     *
     * @param name The name of the custom function
     * @param function The implementation of the function
     * @param description A brief description of what the function does
     * @param extensionHandler An optional function extension handler to process additional instructions
     * @see #registerFunction(Identifier id, String, AIFunction, String, Function, Function)
     */
    public static void registerFunction(Identifier id, String name, AIFunction function, String description, @Nullable Function<String, Object> extensionHandler) throws AwakeApiExceptions.AwakeApiDuplicateLocalFunction, AwakeApiExceptions.AwakeApiDuplicateGlobalFunction {
        AIFunctionManager.registerCustomFunction(id, name, function, description, extensionHandler);
    }

    /**
     * Register multiple custom AI functions at once.
     * @see #registerFunction(Identifier, String, AIFunction, String, Function)
     *
     * @param functions An array of custom functions to register
     * <br>
     * <br>
     * For a detailed breakdown:
     */
    public static void registerFunctions(CustomFunctionData... functions) throws AwakeApiExceptions.AwakeApiDuplicateLocalFunction, AwakeApiExceptions.AwakeApiDuplicateGlobalFunction {
        for (CustomFunctionData function : functions) {
            AIFunctionManager.registerCustomFunction(function.id, function.name, function.function, function.description, function.extensionHandler, function.accessControl);
        }
    }

    /**
     * Register a single custom AI function with access control.
     * <br>
     * <br>
     * Expected structure:
     * <pre>{@code
     * AwakeApiFunctionReg.registerFunction(
     *     "{NAME}",
     *     YourBehaviourImplClass::someMethod,
     *     "Method description. Usage: {NAME}",
     *     (extension) -> YourExtensionHandlerClass::handleExtension,
     *     (accessor) -> YourAccessControlClass::canAccess{NAME}
     * );
     * }</pre>
     * <br>
     * <br>
     * Example usage:
     * <pre>{@code
     * AwakeApiFunctionReg.registerFunction(
     *      "FOLLOW",
     *      AIBehaviours::follow,
     *      "Follow the player. Usage: FOLLOW",
     *      null,
     *      (mobName) -> CustomFunctionAccessors.canFollow(mobName)
     * );
     *
     * AwakeApiFunctionReg.registerFunction(
     *      "ATTACK",
     *      CustomModBehaviours::customAttack,
     *      "Attack a specific mob. Usage: ATTACK_{MOB_NAME}",
     *      (extension) -> CustomMobMaps.getCustomMobClass(extension),
     *      (mobName) -> CustomFunctionAccessors.canAttack(mobName)
     * );
     * }</pre>
     * <p>If a function is registered without an {@code accessControl}
     * function, it will default to applying access to all {@code MobEntity}.
     * In that instance, use the function in see also.</p>
     * <p><b>For more detailed documentation, see also</b></p>
     *
     * @param name The name of the custom function
     * @param function The implementation of the function
     * @param description A brief description of what the function does
     * @param extensionHandler An optional function extension handler to process additional instructions
     * @param accessControl An optional function to determine if a mob has access to this function
     * @see #registerFunction(Identifier, String, AIFunction, String, Function)
     */
    public static void registerFunction(Identifier id, String name, AIFunction function, String description, @Nullable Function<String, Object> extensionHandler, @NotNull Function<Object, Boolean> accessControl) throws AwakeApiExceptions.AwakeApiDuplicateLocalFunction, AwakeApiExceptions.AwakeApiDuplicateGlobalFunction {
        AIFunctionManager.registerCustomFunction(id, name, function, description, extensionHandler, accessControl);
    }

    /**
     * Data class to hold custom function information.
     */
    public record CustomFunctionData(Identifier id, String name, AIFunction function, String description,
                                      @Nullable Function<String, Object> extensionHandler, @Nullable Function<Object, Boolean> accessControl) {
    }
}
