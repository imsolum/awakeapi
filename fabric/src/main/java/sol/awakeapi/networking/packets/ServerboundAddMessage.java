package sol.awakeapi.networking.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.api.api_data.Message;
import sol.awakeapi.interfaces.IPlayerEntity;

public class ServerboundAddMessage {

    private static final String SIMPLE_NAME = ServerboundAddMessage.class.getSimpleName();

    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        AwakeApi.LOGGER.debug("@{}: Packet received", SIMPLE_NAME);

        NbtCompound nbt = buf.readNbt();
        if (nbt != null) {
            Message message = Message.fromNbt(nbt);
            ((IPlayerEntity) player).addMessage(player, message);
        }
    }
}
