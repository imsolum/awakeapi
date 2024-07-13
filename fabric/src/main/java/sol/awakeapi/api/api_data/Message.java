package sol.awakeapi.api.api_data;

import net.minecraft.nbt.NbtCompound;
import java.util.UUID;

/**
 * @param mobUUID    UUID of mob speaking to
 * @param timestamp In-game timestamp
 * @param content   The message content
 * @param isUser    Sent by user or mob?
 */
public record Message(UUID mobUUID, long timestamp, String content, boolean isUser) {

    public NbtCompound toNbt() {
        NbtCompound compound = new NbtCompound();
        compound.putString("MobUUID", this.mobUUID.toString());
        compound.putLong("Timestamp", this.timestamp);
        compound.putString("Content", this.content);
        compound.putBoolean("IsUser", this.isUser);
        return compound;  // Convert to NbtCompound
    }

    public static Message fromNbt(NbtCompound compound) {
        UUID mobUUID = UUID.fromString(compound.getString("MobUUID"));
        long timestamp = compound.getLong("Timestamp");
        String content = compound.getString("Content");
        boolean isUser = compound.getBoolean("IsUser");
        return new Message(mobUUID, timestamp, content, isUser);  // Convert from NbtCompound back to `Message`
    }

    @Override
    public String toString() {
        String sentBy = "You";
        if (!this.isUser) {
            sentBy = "Them";
        }
        return "Message between " + mobUUID + ": " + content + " at: " + timestamp + " sent by " + sentBy;
    }
}