package sol.awakeapi.command;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.Identifier;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.command.type.ModelArgumentType;

@SuppressWarnings("UnreachableCode")
public class AwakeApiCommands {
    public static void registerCommands() {
        ArgumentTypeRegistry.registerArgumentType(
                new Identifier(AwakeApi.MOD_ID, "model_argument_type"),
                ModelArgumentType.class,
                ConstantArgumentSerializer.of(ModelArgumentType::new)
        );
        CommandRegistrationCallback.EVENT.register(SetAI::register);
        CommandRegistrationCallback.EVENT.register(UtilityCommands::register);
    }
}
