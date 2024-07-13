package sol.awakeapi.networking.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.api.api_data.AIParams;
import sol.awakeapi.interfaces.IPlayerEntity;

public class ClientboundUpdateAiParams {
    private static final String SIMPLE_NAME = ClientboundUpdateAiParams.class.getSimpleName();
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        AwakeApi.LOGGER.debug("@{}: Packet received", SIMPLE_NAME);

        ClientPlayerEntity player = client.player;
        assert player != null;

        NbtCompound nbt = buf.readNbt();
        if (nbt != null) {
            AwakeApi.LOGGER.info("@{}: Updating AI data for {}", SIMPLE_NAME, player.getDisplayName().getString());
            AIParams params;
            String type = nbt.getString("type");
            String endpoint = nbt.getString("endpoint");
            if (type.equals("ooba")) {
                params = new AIParams(null, null, endpoint, true);
            } else {
                String key = nbt.getString("key");
                String model = nbt.getString("model");
                params = new AIParams(key, model, endpoint);
            }
            ((IPlayerEntity) player).updateAIParams(player, params);
        }
    }
}
