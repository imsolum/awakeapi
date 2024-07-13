package sol.awakeapi.interfaces;

import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;

public interface ILivingEntity {
    void setDealingWith(@Nullable MobEntity dealingWith);
    @Nullable MobEntity getDealingWith();
}
