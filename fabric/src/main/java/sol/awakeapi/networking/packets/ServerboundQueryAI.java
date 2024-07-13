package sol.awakeapi.networking.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.api.AwakeApiQueryReg;
import sol.awakeapi.api.interfaces.AIQueryHandler;

import java.util.UUID;

public class ServerboundQueryAI {

    private static final String SIMPLE_NAME = ServerboundQueryAI.class.getSimpleName();

    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {

        AwakeApi.LOGGER.warn("@{}: Packet received", SIMPLE_NAME);

        UUID callbackId = buf.readUuid();
        String response = buf.readString();

        AIQueryHandler callback = AwakeApiQueryReg.getCallback(callbackId);
        if (callback != null) {
            callback.handleResponse(response);

            // Clean up the callback
            AwakeApiQueryReg.removeCallback(callbackId);
        } else {
            AwakeApi.LOGGER.warn("@{}: No callback found for ID {}", SIMPLE_NAME, callbackId);
        }
    }
}
