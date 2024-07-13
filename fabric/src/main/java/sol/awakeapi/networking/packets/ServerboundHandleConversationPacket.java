package sol.awakeapi.networking.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.entity.goal.ConverseWithPlayer;
import sol.awakeapi.interfaces.IMobEntity;
import sol.awakeapi.interfaces.IPlayerEntity;

import java.util.UUID;

public class ServerboundHandleConversationPacket {
    private static final String SIMPLE_NAME = ServerboundHandleConversationPacket.class.getSimpleName();
    public static final int DEEP_AQUA = 0x69D6D6;

    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        AwakeApi.LOGGER.debug("@{}: Packet received", SIMPLE_NAME);
        UUID mobUUID = buf.readUuid();
        server.execute(() -> {
            Entity entity = player.getServerWorld().getEntity(mobUUID);
            if (entity instanceof MobEntity) {
                MobEntity speakingTo = ((IPlayerEntity) player).getMobConversingWith();

                if (speakingTo != null) {
                    endConversation(player, speakingTo, true);
                    if (!mobUUID.equals(speakingTo.getUuid())) {
                        startConversation(player, (MobEntity) entity, true);
                    }
                } else {
                    startConversation(player, (MobEntity) entity, true);
                }
            } else {
                AwakeApi.LOGGER.warn("@{}: Could not handle conversation with: {} because UUID received is not a `MobEntity` UUID", SIMPLE_NAME, mobUUID);
            }
        });
    }

    private static void startConversation(ServerPlayerEntity player, MobEntity mob, boolean announce) {
        ((IPlayerEntity) player).converse(player, mob); // Set the conversation with the new mob

        GoalSelector goalSelector = ((IMobEntity) mob).getGoalSelector();


        ConverseWithPlayer converse = new ConverseWithPlayer(mob, player, ConverseWithPlayer.DEFAULT_QUIT_DISTANCE, ConverseWithPlayer.DEFAULT_APPROACH_DISTANCE);
        goalSelector.add(0, converse);

        AwakeApi.LOGGER.info("@{}: {} Started conversation with: {}", player.getDisplayName().getString(), SIMPLE_NAME, mob.getUuid());

        if (announce) {
            Text text = Text.literal("Started conversation with: " + mob.getType().getName().getString() + ". Say hi!").setStyle(Style.EMPTY.withColor(DEEP_AQUA));
            player.networkHandler.sendPacket(new GameMessageS2CPacket(text, true));
        }
    }

    public static void endConversation(ServerPlayerEntity player, MobEntity mob, boolean announce) {
        AwakeApi.LOGGER.info("@{}: Attempting to end conversation with {}", SIMPLE_NAME, mob);
        ((IPlayerEntity) player).converse(player, mob);  // End the existing conversation
        GoalSelector goalSelector = ((IMobEntity) mob).getGoalSelector();
        boolean ended = false;
        for (PrioritizedGoal goal : goalSelector.getGoals()) {
            if (goal.getGoal() instanceof ConverseWithPlayer converseWithPlayer) {
                if (!converseWithPlayer.isFollow()) {
                    converseWithPlayer.deactivate();
                    goalSelector.remove(goal);
                    ended = true;
                }
            }
        }
        if (ended) {
            AwakeApi.LOGGER.info("@{}: {} Ended conversation with: {}", player.getDisplayName().getString(), SIMPLE_NAME, mob.getUuid());

            if (announce) {
                Text text = Text.literal("Ended conversation with: " + mob.getType().getName().getString()).setStyle(Style.EMPTY.withColor(Formatting.RED));
                player.networkHandler.sendPacket(new GameMessageS2CPacket(text, true));
            }
        }
    }
}
