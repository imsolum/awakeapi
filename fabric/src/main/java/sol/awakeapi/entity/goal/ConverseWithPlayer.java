package sol.awakeapi.entity.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import sol.awakeapi.interfaces.IMobEntity;
import sol.awakeapi.networking.packets.ServerboundHandleConversationPacket;

/**
 * Could be replaced by in-game TemptGoal
 * @see net.minecraft.entity.ai.goal.TemptGoal
 */
public class ConverseWithPlayer extends Goal {
    private final MobEntity mob;
    private final ServerPlayerEntity player;
    private boolean isActive;
    private final double quitDistance;
    private final double approachDistance;
    public static final double DEFAULT_QUIT_DISTANCE = 64;
    public static final double DEFAULT_APPROACH_DISTANCE = 6;
    public static final double FOLLOW = Double.MAX_VALUE;

    public ConverseWithPlayer(MobEntity mob, ServerPlayerEntity player, double quitDistance, double approachDistance) {
        this.mob = mob;
        this.player = player;
        this.isActive = true;
        this.quitDistance = quitDistance;
        this.approachDistance = approachDistance;
    }

    @Override
    public boolean canStart() {
        return this.isActive;
    }

    @Override
    public void start() {
        // Stop mob movement and look at player
        this.mob.getNavigation().stop();
        this.mob.getLookControl().lookAt(player);
    }

    public void deactivate() {
        this.isActive = false;
    }

    @Override
    public void tick() {
        if (!((IMobEntity) mob).isBusy()) {
            double distanceSquared = this.mob.squaredDistanceTo(player.getX(), player.getY(), player.getZ());
            if (quitDistance != Double.MAX_VALUE && distanceSquared >= quitDistance) { // default value is 64, implying 8 blocks
                this.isActive = false;
                ServerboundHandleConversationPacket.endConversation(player, mob, true);
                return;
            }
            if (distanceSquared >= approachDistance) { // default value is 6, implying ~2.5 blocks
                if (!isTraderOccupied() && !isSitting()) {
                    this.mob.getNavigation().startMovingTo(player, 1.1D);
                    this.mob.getLookControl().lookAt(player);
                }
            }
            this.mob.getLookControl().lookAt(player); // Runs regardless of distance (minus distance > 8 blocks)
        }
    }

    private boolean isTraderOccupied() {
        return this.mob instanceof MerchantEntity merchant && merchant.getCustomer() != null;
    }

    private boolean isSitting() {
        return this.mob instanceof TameableEntity tameable && tameable.isSitting();
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public boolean isFollow() {
        return this.quitDistance == Double.MAX_VALUE;
    }

}
