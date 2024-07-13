package sol.awakeapi.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sol.awakeapi.entity.data.Interaction;
import sol.awakeapi.interfaces.ILivingEntity;
import sol.awakeapi.interfaces.IMobEntity;
import sol.awakeapi.networking.packets.ServerboundHandleConversationPacket;
import sol.awakeapi.util.InteractionType;

@SuppressWarnings("UnreachableCode")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingEntity {

    private @Nullable MobEntity dealingWith = null;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
        LivingEntity self = (LivingEntity)(Object)this;  // Explicitly cast this to the target class

        // Check if the instance is a PlayerEntity
        if (self instanceof ServerPlayerEntity player && source.getAttacker() instanceof MobEntity mob) {
            ((IMobEntity) mob).updateEnvironmentInfo(new Interaction(InteractionType.ATTACKED_PLAYER, self.getUuid(), "You hurt: " + self.getDisplayName().getString()));
        }
        // Check if the instance is a MobEntity
        else if (self instanceof MobEntity && source.getAttacker() instanceof ServerPlayerEntity player) {
            ((IMobEntity) self).updateEnvironmentInfo(new Interaction(InteractionType.ATTACKED_BY_PLAYER, player.getUuid(), "You were hurt by: " + player.getDisplayName().getString()));
        }
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    public void onHeal(float amount, CallbackInfo ci) {
        if (amount > 0 && this instanceof IMobEntity) {  // Check if this is an instance of IMobEntity
            ((IMobEntity) this).updateEnvironmentInfo(new Interaction(InteractionType.HEALED, null, "You healed (could have been self or by player)"));  // Safely cast to IMobEntity now
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        if (dealingWith != null) {
            ((IMobEntity) dealingWith).freeBusy(false);
            setDealingWith(null);
        }

        LivingEntity livingEntity = (LivingEntity)(Object)this;
        if (livingEntity instanceof MobEntity mob) {
            ServerPlayerEntity player = ((IMobEntity) mob).getConversingWith();
            if (player != null) {
                ServerboundHandleConversationPacket.endConversation(player, mob, true);
            }
        }
    }

    public @Nullable MobEntity getDealingWith() {
        return dealingWith;
    }

    public void setDealingWith(@Nullable MobEntity dealingWith) {
        this.dealingWith = dealingWith;
    }
}
