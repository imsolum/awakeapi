package sol.awakeapi.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.interfaces.IPlayerEntity;

public class UtilityCommands {
    
    private static final String SIMPLE_NAME = UtilityCommands.class.getSimpleName();

    public static void register(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        LiteralCommandNode<ServerCommandSource> awakeNode = serverCommandSourceCommandDispatcher.register(
                CommandManager.literal("awakeapi")
                        .then(CommandManager.literal("clear")
                                .executes(UtilityCommands::executeClear))
                        .then(CommandManager.literal("clearAll")
                                .executes(UtilityCommands::executeClearAll))
                        .then(CommandManager.literal("json")
                                .executes(UtilityCommands::asJson))
        );
    }

    private static int asJson(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        assert player != null;
        MobEntity mob = ((IPlayerEntity) player).getMobConversingWith();
        if (mob != null) {
            JsonObject jsonObject = ((IPlayerEntity) player).asArray(mob.getUuid());
            if (jsonObject != null) {
                source.sendFeedback(() -> Text.literal("Displayed conversation with current mob as JSON in server logs."), false);
                AwakeApi.LOGGER.info("@{}: {}", SIMPLE_NAME, jsonObject);
                return 1;
            } else {
                source.sendFeedback(() -> Text.literal("Uhh.. an impossible error has occured. Logging info... :("), false);
                AwakeApi.LOGGER.error("@{}: `asJson` can only return a value when speakingTo != null (which this seemed to be), but `asArray` returned null implying the opposite.", SIMPLE_NAME);
                return 0;
            }
        } else {
            AwakeApi.LOGGER.warn("@{}: Player: `{}` attempted to display conversation as JSON but is not in a conversation with any mobs. No conversations viewed.", SIMPLE_NAME, player.getDisplayName().getString());
            source.sendFeedback(() -> Text.literal("Could not display conversation with current mob. Note: Must be engaged in a conversation with a mob to use"), false);
        }
        return 0;
    }

    private static int executeClearAll(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        assert player != null;
        ((IPlayerEntity) player).clearAllMessages();
        AwakeApi.LOGGER.info("@{}: Cleared all conversations with all mobs for player: `{}`", SIMPLE_NAME, player.getDisplayName().getString());
        source.sendFeedback(() -> Text.literal("Cleared all conversations with all mobs"), false);
        return 1;
    }

    private static int executeClear(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        assert player != null;
        MobEntity mob = ((IPlayerEntity) player).getMobConversingWith();
        if (mob != null) {
            ((IPlayerEntity) player).clearMessages(mob.getUuid());
            source.sendFeedback(() -> Text.literal("Cleared conversation with current mob"), false);
            return 1;
        } else {
            AwakeApi.LOGGER.warn("@{}: Player: `{}` attempted to clear conversations but is not in a conversation with any mobs. No conversations cleared.", SIMPLE_NAME, player.getDisplayName().getString());
            source.sendFeedback(() -> Text.literal("Could not clear conversation with current mob. Note: Must be engaged in a conversation with a mob to use"), false);
        }
        return 0;
    }
}
