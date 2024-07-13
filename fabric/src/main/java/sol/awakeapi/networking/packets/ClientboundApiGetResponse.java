package sol.awakeapi.networking.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.api.AwakeApiImpl;
import sol.awakeapi.api.api_data.AIData;

public class ClientboundApiGetResponse {

    private static final String SIMPLE_NAME = ClientboundApiGetResponse.class.getSimpleName();

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        AwakeApi.LOGGER.debug("@{}: Packet received", SIMPLE_NAME);
        assert client.player != null;

        NbtCompound compound = buf.readNbt();
        if (compound != null) {
            AwakeApi.LOGGER.info("@{}: Received AIData pertaining to {}. Requesting response...", SIMPLE_NAME, client.player.getDisplayName().getString());

            AIData data = AIData.fromNbt(compound);
            AwakeApiImpl.getResponse(client.player, data);
        } else {
            AwakeApi.LOGGER.error("@{}: Attempted to interact with AI API but {} did not provide any AIData.", SIMPLE_NAME, client.player.getDisplayName().getString() );
        }
    }
}
