package sol.awakeapi.networking.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.api.AwakeApiImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientboundQueryAI {

    private static final String SIMPLE_NAME = ClientboundQueryAI.class.getSimpleName();

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        AwakeApi.LOGGER.debug("@{}: Packet received", SIMPLE_NAME);

        int size = buf.readInt();
        if (size > 1) {
            List<String> systemMessages = new ArrayList<>();
            for (int i = 1; i < size; i++) {
                systemMessages.add(buf.readString());
            }

            UUID callbackId = buf.readUuid();

            AwakeApiImpl.queryAI(client.player, systemMessages, callbackId);
        } else if (size == 1) {
            String systemMessage = buf.readString();
            boolean isImage = buf.readBoolean();

            UUID callbackId = buf.readUuid();

            AwakeApiImpl.queryAI(client.player, systemMessage, isImage, callbackId);
        } else {
            AwakeApi.LOGGER.warn("@{}: Attempted to query AI with 0 system messages", SIMPLE_NAME);
        }
    }
}
