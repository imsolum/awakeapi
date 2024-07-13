package sol.awakeapi.interfaces;

import com.google.gson.JsonObject;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import sol.awakeapi.api.api_data.AIData;
import sol.awakeapi.api.api_data.AIParams;
import sol.awakeapi.api.api_data.Message;
import sol.awakeapi.util.ConverseStatus;

import java.util.List;
import java.util.UUID;

public interface IPlayerEntity {
    ConverseStatus converse(ServerPlayerEntity player, MobEntity mob);
    MobEntity getMobConversingWith();
    boolean addMessage(ServerPlayerEntity ignoredPlayer, Message msg);
    List<Message> getMessages(UUID uuid);
    boolean clearMessages(UUID uuid);
    void clearAllMessages();
    AIData getAIData(@Nullable String systemMessage, boolean isImage);
    AIData getAIData();
    AIData getAIData(MobEntity mob, @Nullable String systemMessage, boolean isImage);
    AIData getAIData(MobEntity mob);
    void updateAIParams(ClientPlayerEntity player, AIParams aiParams);
    AIParams getAIParams();
    JsonObject asArray(UUID mobUuid);
}
