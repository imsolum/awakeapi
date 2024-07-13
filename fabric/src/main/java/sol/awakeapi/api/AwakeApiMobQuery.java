package sol.awakeapi.api;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import sol.awakeapi.entity.data.Interaction;
import sol.awakeapi.interfaces.IMobEntity;

import java.util.List;

/**
 * Utility class providing useful methods
 * around {@code MobEntity}'s
 */
public class AwakeApiMobQuery {


    /**
     * This method should be used when a mob becomes busy with a
     * task that involves another mob. Typically, this would be
     * useful because, by default, an ongoing conversation will
     * force a mob to follow a player if they are not busy.
     * @implNote By default, {@code freeBusy(false)} will be called
     * when the {@code MobEntity busyWith} dies.
     * @param busyMob The mob that will become busy
     * @param busyWith The mob that the main mob is busy with
     * @param announceTo A {@code Nullable} field that denotes
     *                   if the mob should tell they player
     *                   when they're done. For group tasks,
     *                   this should be {@code null}.
     */
    public static void makeBusy(MobEntity busyMob, MobEntity busyWith, @Nullable ServerPlayerEntity announceTo) {
        ((IMobEntity) busyMob).makeBusy(busyWith, announceTo);
    }

    /**
     * A method to free up a mob, denoting it has completed a task.
     * If {@code force} is set to true, the mob will not announce
     * anything.
     * @param mob The mob to free
     * @param force Flag denoting whether to announce completion to
     *              the mob (which in turn will tell the player)
     */
    public static void freeBusy(MobEntity mob, boolean force) {
        ((IMobEntity) mob).freeBusy(force);
    }

    public static boolean isBusy(MobEntity mob) {
        return ((IMobEntity) mob).isBusy();
    }

    /**
     * This method wil add an {@code Interaction} to the mob's
     * environment history. By default, this method is called
     * every 1.5 minutes (real time), so this method should only
     * externally be used to add a custom Interaction.
     * @param mob
     * @param interaction
     * @see Interaction
     */
    public static void updateEnvironmentInfo(MobEntity mob, @Nullable Interaction interaction) {
        ((IMobEntity) mob).updateEnvironmentInfo(interaction);
    }

    /**
     * A method that returns a {@code Nullable ServerPlayerEntity} object of whom
     * the mob is currently engaged in a conversation with.
     * @param mob
     * @return {@code Nullable ServerPlayerEntity}
     */
    public static @Nullable ServerPlayerEntity getPlayerConversingWith(MobEntity mob) {
        return ((IMobEntity) mob).getConversingWith();
    }

    /**
     * This method will update the registered {@code ServerPlayerEntity} a mob is
     * conversing with.
     * @implNote Ideally, this method should not be called manually, as it is controlled
     * by the player conversation manager.
     * @param mob
     * @param player
     * @see sol.awakeapi.mixin.PlayerEntityMixin#converse(ServerPlayerEntity, MobEntity)
     */
    public static void setConversingWith(MobEntity mob, @Nullable ServerPlayerEntity player) {
        ((IMobEntity) mob).setConversingWith(player);
    }

    /**
     * This method returns the emotional characteristics of a mob
     * @param mob
     * @return a {@code List<String>} of emotional / personality traits
     */
    public static List<String> getEmotions(MobEntity mob) {
        return ((IMobEntity) mob).getEmotionalStates();
    }
}
