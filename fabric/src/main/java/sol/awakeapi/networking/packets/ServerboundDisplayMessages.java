package sol.awakeapi.networking.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.api.api_data.Message;
import sol.awakeapi.interfaces.IPlayerEntity;
import sol.awakeapi.util.Formatter;

import java.util.List;

public class ServerboundDisplayMessages {

    private final static String SIMPLE_NAME = ServerboundDisplayMessages.class.getSimpleName();

    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        AwakeApi.LOGGER.info("@{}: Packet(?) received", SIMPLE_NAME);
        MobEntity speakingTo = ((IPlayerEntity) player).getMobConversingWith();

        if (speakingTo != null) {
            List<Message> messages = ((IPlayerEntity) player).getMessages(speakingTo.getUuid());
            for (Message message : messages) {
                Formatting color = message.isUser() ? Formatting.GREEN : Formatting.BLUE;
                Text text = Text.literal(Formatter.getValidReplyFromJson(message.content())).setStyle(Style.EMPTY.withColor(color));
                player.sendMessage(text, false);
            }

            if (messages.isEmpty()) {
                Text text = Text.literal("No messages found with specified mob");
                player.sendMessage(text, false);
            }
        } else {
            player.sendMessage(Text.literal("Cannot display messages when not currently speaking to any mob"));
            AwakeApi.LOGGER.warn("@{}: {} attempted to display messages but not currently speaking to any mob.", SIMPLE_NAME, player.getDisplayName().getString());
        }
    }
}
