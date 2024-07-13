package sol.awakeapi;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sol.awakeapi.api.AwakeApiImpl;
import sol.awakeapi.api.api_data.Message;
import sol.awakeapi.command.AwakeApiCommands;
import sol.awakeapi.interfaces.IPlayerEntity;
import sol.awakeapi.networking.AwakeApiPackets;

public class AwakeApi implements ModInitializer {
	public static final String MOD_ID = "awakeapi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("@{}: The world breathes life....", AwakeApi.class.getSimpleName());
		AwakeApiPackets.registerC2SPackets();
		AwakeApiCommands.registerCommands();
		registerChatEvents();
	}

	private void registerChatEvents() {
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((content, sender, params) -> {
			MobEntity speakingTo = ((IPlayerEntity) sender).getMobConversingWith();
			String signedContent = content.getSignedContent();
			if (speakingTo != null) {
				addMessage(sender, speakingTo, signedContent);
				return false;
			}
			return true; // Allow the chat message to be processed normally if not speaking to any mob
		});
	}

	private void addMessage(ServerPlayerEntity sender, MobEntity speakingTo, String messageContent) {
		long currentTime = sender.getWorld().getTimeOfDay();
		Message message = new Message(speakingTo.getUuid(), currentTime, messageContent, true);
		((IPlayerEntity) sender).addMessage(sender, message);
		AwakeApiImpl.getResponse(sender);
	}
}