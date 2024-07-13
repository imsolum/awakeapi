package sol.awakeapi.entity.data;

import net.minecraft.nbt.NbtCompound;
import sol.awakeapi.util.InteractionType;
import java.util.UUID;

public class Interaction {
    private final InteractionType interactionType;
    private final UUID playerUuid;
    private final String interactionDetails;

    public Interaction(InteractionType interactionType, UUID playerUuid, String interactionDetails) {
        this.interactionType = interactionType;
        this.playerUuid = playerUuid;
        this.interactionDetails = interactionDetails;
    }

    public String getInteractionDetails() {
        return interactionDetails;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("InteractionType", interactionType.toString());
        if (playerUuid != null) {
            nbt.putUuid("PlayerUuid", playerUuid);
        }
        nbt.putString("InteractionDetails", interactionDetails);
        return nbt;
    }

    public static Interaction fromNbt(NbtCompound nbt) {
        InteractionType type = InteractionType.valueOf(nbt.getString("InteractionType"));
        UUID playerUuid = null;
        if (nbt.contains("PlayerUuid")) {
            playerUuid = nbt.getUuid("PlayerUuid");
        }
        String interactionDetails = nbt.getString("InteractionDetails");
        return new Interaction(type, playerUuid, interactionDetails);
    }

    @Override
    public String toString() {
        return "InteractionType: " + interactionType.toString() +
                "\nWith playerUuid: " + playerUuid +
                "\nInteractionDetails: " + interactionDetails;
    }
}
