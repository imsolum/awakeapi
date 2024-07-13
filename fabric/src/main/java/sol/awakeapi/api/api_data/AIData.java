package sol.awakeapi.api.api_data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.Nullable;
import sol.awakeapi.entity.data.Emotion;
import sol.awakeapi.entity.data.History;
import java.util.*;

public record AIData(UUID entityUuid, List<Message> messages, List<History> histories, List<String> emotions, String roles, List<String> accessibleFunctions, @Nullable String systemMessage, @Nullable Boolean isImage) {

    public NbtCompound toNbt() {
        NbtCompound compound = new NbtCompound();
        compound.putUuid("EntityUuid", entityUuid);
        compound.put("Messages", messagesToNbt());
        compound.put("Histories", historiesToNbt());
        compound.put("Emotions", emotionsToNbt());
        compound.putString("Roles", roles);
        compound.put("AccessibleFunctions", accessibleFunctionsToNbt(accessibleFunctions));
        if (systemMessage != null) {
            compound.putString("SystemMessage", systemMessage);
        }
        if (isImage != null) {
            compound.putBoolean("isImage", isImage);
        }
        return compound;  // Convert to NbtCompound
    }

    public static AIData fromNbt(NbtCompound compound) {
        UUID entityUuid = compound.getUuid("EntityUuid");
        List<Message> messages = messagesFromNbt(compound.getList("Messages", 10)); // 10 is the ID for NbtCompound
        List<History> histories = historiesFromNbt(compound.getList("Histories", 10));
        List<String> emotions = Emotion.fromNbt(compound.getCompound("Emotions"));
        String roles = compound.getString("Roles");
        List<String> accessibleFunctions = accessibleFunctionsFromNbt(compound.getCompound("AccessibleFunctions"));
        String systemMessage = compound.contains("SystemMessage") ? compound.getString("SystemMessage") : null;
        Boolean isImage = compound.contains("isImage") ? compound.getBoolean("isImage") : null;
        return new AIData(entityUuid, messages, histories, emotions, roles, accessibleFunctions, systemMessage, isImage);
    }

    public NbtList messagesToNbt() {
        NbtList list = new NbtList();
        if (messages != null) {
            for (Message message : messages) {
                list.add(message.toNbt());
            }
        }
        return list;
    }

    public NbtList historiesToNbt() {
        NbtList list = new NbtList();
        if (histories != null) {
            for (History history : histories) {
                list.add(history.toNbt());
            }
        }
        return list;
    }

    public static NbtCompound accessibleFunctionsToNbt(List<String> functions) {
        NbtCompound compound = new NbtCompound();
        NbtList nbtList = new NbtList();
        for (String function : functions) {
            nbtList.add(NbtString.of(function));
        }
        compound.put("Functions", nbtList);
        return compound;
    }

    public NbtCompound emotionsToNbt() {
        return Emotion.toNbt(emotions);
    }

    private static List<Message> messagesFromNbt(NbtList nbtList) {
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound compound = nbtList.getCompound(i);
            messages.add(Message.fromNbt(compound));
        }
        return messages;
    }

    private static List<History> historiesFromNbt(NbtList nbtList) {
        List<History> histories = new ArrayList<>();
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound compound = nbtList.getCompound(i);
            histories.add(History.fromNbt(compound));
        }
        return histories;
    }

    public static List<String> accessibleFunctionsFromNbt(NbtCompound compound) {
        NbtList nbtList = compound.getList("Functions", 8);
        List<String> functions = new ArrayList<>();
        for (int i = 0; i < nbtList.size(); i++) {
            functions.add(nbtList.getString(i));
        }
        return functions;
    }
}
