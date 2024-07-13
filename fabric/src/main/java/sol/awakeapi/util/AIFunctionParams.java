package sol.awakeapi.util;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sol.awakeapi.AwakeApi;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record AIFunctionParams(MobEntity currentMob, @Nullable Object extension,
                               ServerPlayerEntity serverPlayerEntity, boolean group) {
    private static final String SIMPLE_NAME = AIFunctionParams.class.getSimpleName();

    public static @Nullable Team getFriendlyTeam(@NotNull ServerPlayerEntity player) {
        ServerScoreboard scoreboard = player.getServer().getScoreboard();
        String teamName = "mobs_friendly_with_" + player.getDisplayName().getString();
        ;
        return scoreboard.getTeam(teamName);
    }

    public static Set<UUID> getFriendlyMobUUIDs(@NotNull ServerPlayerEntity player) {
        Set<UUID> friendlyMobs = new HashSet<>();
        Team friendlyTeam = getFriendlyTeam(player);
        if (friendlyTeam != null) {
            for (String uuidString : friendlyTeam.getPlayerList()) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    friendlyMobs.add(uuid);
                } catch (IllegalArgumentException e) {
                    if (!Objects.equals(uuidString, player.getDisplayName().getString())) {
                        AwakeApi.LOGGER.warn("@{}: Invalid UUID string: {}. Skipping...", SIMPLE_NAME, uuidString);
                    }
                }
            }
        }
        return friendlyMobs;
    }

    @Override
    public String toString() {
        return "AIFunctionParams{" +
                "currentMob=" + currentMob +
                ", extension=" + extension +
                ", serverPlayerEntity=" + serverPlayerEntity +
                ", group=" + group +
                '}';
    }
}
