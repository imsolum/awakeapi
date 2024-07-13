package sol.awakeapi;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import sol.awakeapi.keybindings.AwakeApiKeybindings;
import sol.awakeapi.networking.AwakeApiPackets;
import sol.awakeapi.util.AwakeApiAsyncRequest;

import java.util.UUID;

public class AwakeApiClient implements ClientModInitializer {

    private final static String SIMPLE_NAME = AwakeApiClient.class.getSimpleName();
    private static long displayMessagesLastPressed = 0;

    @Override
    public void onInitializeClient() {
        AwakeApiAsyncRequest.registerHttpClients();
        AwakeApiPackets.registerS2CPackets();
        AwakeApiKeybindings.initialise();
        registerTickEvents();
    }

    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register((client -> {
            assert client.player != null;

            while (AwakeApiKeybindings.SPEAK_TO_MOB.wasPressed()) {
                AwakeApi.LOGGER.info( "@{}: {} triggered handleMobHit.", SIMPLE_NAME, client.player.getDisplayName().getString());
                if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHitResult = (EntityHitResult) client.crosshairTarget;
                    Entity targetedEntity = entityHitResult.getEntity();
                    if (targetedEntity instanceof MobEntity) {
                        handleMobHit(targetedEntity.getUuid());
                    }
                }
            }

            if (AwakeApiKeybindings.DISPLAY_MESSAGES.isPressed()) {
                // Check time since last pressed
                assert client.world != null; // Tick events only happen in-game
                long pressed = client.world.getTimeOfDay();
                if (pressed > displayMessagesLastPressed + 30) {
                    displayMessagesLastPressed = pressed;

                    ClientPlayNetworking.send(AwakeApiPackets.SERVERBOUND_DISPLAY_MESSAGES, PacketByteBufs.empty());
                }
            }
        }));
    }

    /**
     * Sends mob uuid to the server.
     * <p>
     * Server needs uuid in order to control mob behaviour
     *       as well as saving conversation related data
     * @param mobUuid Mob's UUID
     */
    private void handleMobHit(UUID mobUuid) {
        PacketByteBuf buf = PacketByteBufs.create().writeUuid(mobUuid);
        ClientPlayNetworking.send(AwakeApiPackets.SERVERBOUND_HANDLE_CONVERSATION, buf);
    }
}
