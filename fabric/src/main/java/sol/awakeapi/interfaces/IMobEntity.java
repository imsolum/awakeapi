package sol.awakeapi.interfaces;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import sol.awakeapi.entity.data.History;
import sol.awakeapi.entity.data.Interaction;

import java.util.List;

public interface IMobEntity {
    GoalSelector getGoalSelector();
    boolean isBusy();
    void makeBusy(MobEntity busyWith, @Nullable ServerPlayerEntity announceTo);
    void freeBusy(boolean force);
    List<String> getEmotionalStates();
    void updateEnvironmentInfo(@Nullable Interaction interaction);
    String getRoles();
    List<History> getAllHistory();
    void setConversingWith(@Nullable ServerPlayerEntity conversingWith);
    @Nullable ServerPlayerEntity getConversingWith();
}
