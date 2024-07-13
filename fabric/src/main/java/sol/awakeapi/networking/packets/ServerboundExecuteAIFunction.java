package sol.awakeapi.networking.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.entity.data.api.AIFunctionManager;
import sol.awakeapi.entity.data.api.interfaces.AIFunctionBase;
import sol.awakeapi.util.AIFunctionParams;

import java.util.UUID;

public class ServerboundExecuteAIFunction {

    private static final String SIMPLE_NAME = ServerboundExecuteAIFunction.class.getSimpleName();

    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        AwakeApi.LOGGER.debug("@{}: Packet received", SIMPLE_NAME);

        // Obtain information pertaining to AIFunction
        UUID uuid = buf.readUuid();
        String function = buf.readString();
        String functionExtension = buf.readString();
        boolean isGroup = buf.readBoolean();

        MobEntity mob = (MobEntity) player.getServerWorld().getEntity(uuid);

        // Create params
        AIFunctionBase functions = AIFunctionManager.getFunction(function);
        Object extensionResult = functions.handleFunctionExtension(functionExtension);

        AIFunctionParams params = new AIFunctionParams(mob, extensionResult, player, isGroup);

        // Execute
        functions.run(params);
    }
}
