package sol.awakeapi.mixin;


import com.google.gson.JsonObject;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sol.awakeapi.AwakeApi;
import sol.awakeapi.api.api_data.AIData;
import sol.awakeapi.api.api_data.AIParams;
import sol.awakeapi.api.api_data.Message;
import sol.awakeapi.entity.data.History;
import sol.awakeapi.entity.data.api.AIFunctionManager;
import sol.awakeapi.interfaces.IMobEntity;
import sol.awakeapi.interfaces.IPlayerEntity;
import sol.awakeapi.util.ConverseStatus;
import sol.awakeapi.util.Formatter;

import java.util.*;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IPlayerEntity {

    private MobEntity speakingTo = null;
    private final Map<UUID, List<Message>> conversations = new HashMap<>();
    private AIParams aiParams = new AIParams(null, null, null, false);
    
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    public boolean addMessage(ServerPlayerEntity ignoredPlayer, Message msg) {
        UUID key = msg.mobUUID();
        boolean keyExists = conversations.containsKey(key);
        conversations.computeIfAbsent(key, k -> new ArrayList<>()).add(msg);
        return keyExists;
    }

    public List<Message> getMessages(UUID uuid) {
        return conversations.getOrDefault(uuid, new ArrayList<>());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.put("Conversations", conversationsToNbt());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        conversationsFromNbt(nbt);
    }

    private NbtCompound conversationsToNbt() {
        NbtCompound compound = new NbtCompound();
        NbtCompound conversationData = new NbtCompound();
        for (Map.Entry<UUID, List<Message>> entry : conversations.entrySet()) {
            NbtList messagesList = new NbtList();
            for (Message msg : entry.getValue()) {
                messagesList.add(msg.toNbt());
            }
            conversationData.put(entry.getKey().toString(), messagesList);
        }
        compound.put("Conversations", conversationData);
        return compound;
    }

    private void conversationsFromNbt(NbtCompound compound) {
        if (compound.contains("Conversations", NbtElement.COMPOUND_TYPE)) {
            NbtCompound conversationData = compound.getCompound("Conversations").getCompound("Conversations");
            for (String key : conversationData.getKeys()) {
                try {
                    UUID mobUUID = UUID.fromString(key);  // Correctly parse the UUID from the keys
                    NbtList messageList = conversationData.getList(key, NbtElement.COMPOUND_TYPE);
                    List<Message> messages = new ArrayList<>();
                    for (int i = 0; i < messageList.size(); i++) {
                        messages.add(Message.fromNbt(messageList.getCompound(i)));
                    }
                    conversations.put(mobUUID, messages);
                } catch (IllegalArgumentException e) {
                    AwakeApi.LOGGER.error("@{}: Error parsing UUID from NBT: {}", PlayerEntityMixin.class.getSimpleName(),  key);
                }
            }
        }
    }

    public void clearAllMessages() {
        conversations.clear();
    }

    public boolean clearMessages(UUID uuid) {
        return conversations.remove(uuid) != null;
    }

    public ConverseStatus converse(ServerPlayerEntity player, MobEntity mob) {
        if (this.speakingTo == mob) {
            this.speakingTo = null;
            ((IMobEntity) mob).setConversingWith(player);
            return ConverseStatus.ENDED;
        }

        this.speakingTo = mob;
        ((IMobEntity) mob).setConversingWith(player);
        return (this.speakingTo == null) ? ConverseStatus.STARTED : ConverseStatus.REPLACED;
    }

    public MobEntity getMobConversingWith() {
        return this.speakingTo;
    }

    public JsonObject asArray(UUID mobUuid) {
        if (this.speakingTo != null) {
            String mobName = this.speakingTo.getType().getName().getString();
            long currentTime = this.getWorld().getTimeOfDay();

            return Formatter.formatMessagesForAI(getAIData(speakingTo), null, mobName, currentTime);
        }
        return null;
    }

    public AIData getAIData(@Nullable String systemMessage, boolean isImage) {
        if (speakingTo != null) {
            String mobName = speakingTo.getType().getName().getString();
            List<Message> messages = getMessages(speakingTo.getUuid());
            List<History> histories = ((IMobEntity) speakingTo).getAllHistory();
            List<String> emotionalStates = ((IMobEntity) speakingTo).getEmotionalStates();
            String roles = ((IMobEntity) speakingTo).getRoles();

            return new AIData(speakingTo.getUuid(), messages, histories, emotionalStates, roles, AIFunctionManager.getAllFunctions(mobName), systemMessage, isImage);
        }
        return null;
    }

    public AIData getAIData() {
        if (speakingTo != null) {
            String mobName = speakingTo.getType().getName().getString();
            List<Message> messages = getMessages(speakingTo.getUuid());
            List<History> histories = ((IMobEntity) speakingTo).getAllHistory();
            List<String> emotionalStates = ((IMobEntity) speakingTo).getEmotionalStates();
            String roles = ((IMobEntity) speakingTo).getRoles();

            return new AIData(speakingTo.getUuid(), messages, histories, emotionalStates, roles, AIFunctionManager.getAllFunctions(mobName), null, null);
        }
        return null;
    }

    public AIData getAIData(MobEntity mob, @Nullable String systemMessage, boolean isImage) {
        String mobName = mob.getType().getName().getString();
        List<Message> messages = getMessages(mob.getUuid());
        List<History> histories = ((IMobEntity) mob).getAllHistory();
        List<String> emotionalStates = ((IMobEntity) mob).getEmotionalStates();
        String roles = ((IMobEntity) mob).getRoles();

        return new AIData(mob.getUuid(), messages, histories, emotionalStates, roles, AIFunctionManager.getAllFunctions(mobName), systemMessage, isImage);
    }

    public AIData getAIData(MobEntity mob) {
        String mobName = mob.getType().getName().getString();
        List<Message> messages = getMessages(mob.getUuid());
        List<History> histories = ((IMobEntity) mob).getAllHistory();
        List<String> emotionalStates = ((IMobEntity) mob).getEmotionalStates();
        String roles = ((IMobEntity) mob).getRoles();

        return new AIData(mob.getUuid(), messages, histories, emotionalStates, roles, AIFunctionManager.getAllFunctions(mobName), null, null);
    }

    public void updateAIParams(ClientPlayerEntity ignoredClientPlayer, AIParams aiParams) {
        this.aiParams = aiParams;
    }

    public AIParams getAIParams() {
        return this.aiParams;
    }
}
