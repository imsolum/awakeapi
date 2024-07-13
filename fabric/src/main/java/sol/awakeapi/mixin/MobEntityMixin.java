package sol.awakeapi.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sol.awakeapi.api.AwakeApiImpl;
import sol.awakeapi.entity.data.Emotion;
import sol.awakeapi.entity.data.History;
import sol.awakeapi.entity.data.Interaction;
import sol.awakeapi.entity.data.Role;
import sol.awakeapi.interfaces.ILivingEntity;
import sol.awakeapi.interfaces.IMobEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnreachableCode")
@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements IMobEntity {
    private static final String SIMPLE_NAME = MobEntityMixin.class.getSimpleName();

    private boolean isBusy = false;
    private ServerPlayerEntity announceTo = null;
    private long sinceSave = 0;
    private List<History> allHistory;
    private final int MAX_HISTORY_ELEMENTS = 35;
    private List<String> emotionalStates;
    private @Nullable ServerPlayerEntity conversingWith = null;

    @Shadow
    GoalSelector goalSelector;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (allHistory != null) {
            NbtList historyList = new NbtList();
            for (History history : allHistory) {
                historyList.add(history.toNbt());
            }
            nbt.put("MobHistory", historyList);
        }

        if (emotionalStates != null) {
            String emotions = String.join("<end_emotion>", emotionalStates);
            nbt.putString("EmotionalStates", emotions);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("MobHistory", 9)) { // 9 for list tag
            NbtList historyList = nbt.getList("MobHistory", 10); // 10 for compound tag
            allHistory = new ArrayList<>();
            for (int i = 0; i < historyList.size(); i++) {
                NbtCompound historyNbt = historyList.getCompound(i);
                allHistory.add(History.fromNbt(historyNbt));
            }
        }

        if (nbt.contains("EmotionalStates", NbtElement.STRING_TYPE)) {
            String emotions = nbt.getString("EmotionalStates");
            emotionalStates = List.of(emotions.split("<end_emotion>"));
        } else {
            emotionalStates = Emotion.getRandomEmotions(2, 3);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
        if (sinceSave + 1800 < this.getWorld().getTimeOfDay()) {
            // 1.5 minutes have passed (or mob loaded into world)
            sinceSave = this.getWorld().getTimeOfDay();
            updateEnvironmentInfo(null);
        }
    }

    public void updateEnvironmentInfo(@Nullable Interaction interaction) {
        MobEntity mob = (MobEntity)(Object)this;

        // Get current biome
        Biome biome = this.getWorld().getBiome(this.getBlockPos()).value();
        Identifier biomeId = this.getWorld().getRegistryManager().get(RegistryKeys.BIOME).getId(biome);
        assert biomeId != null;
        String biomeName = biomeId.toString();

        // Get current weather
        boolean isRaining = mob.getWorld().isRaining();
        boolean isThundering = mob.getWorld().isThundering();
        String weather = isThundering ? "thundering" : (isRaining ? "raining / snowing" : "clear");

        // Get current date (i.e. day/hour e.g. 11/08:00)
        long worldTime = mob.getWorld().getTimeOfDay();
        int days = (int)(worldTime / 24000L);
        int hours = (int)((worldTime % 24000L) / 1000) + 6; // Minecraft starts at 6 AM
        hours = hours > 24 ? hours - 24 : hours;
        String dateTime = String.format("%d/%02d:00", days, hours);

        // Get current age (specifically "baby" or "adult")
        boolean isBaby = mob.isBaby();

        // Get current status effects
        String flattenedStatusEffects = mob.getStatusEffects().stream()
                .map(StatusEffectInstance::toString)
                .collect(Collectors.joining(", "));

        // Get current health percentage
        double healthPercentage = Math.floor((mob.getHealth() / mob.getMaxHealth()) * 100);

        // Add to mob's history
        History history = new History(biomeName, weather, isBaby, flattenedStatusEffects, healthPercentage, interaction, dateTime);
        updateHistory(history);
    }

    private void updateHistory(History history) {
        if (allHistory == null) {
            allHistory = new ArrayList<>();
        }
        if (allHistory.size() == MAX_HISTORY_ELEMENTS) {
            allHistory.remove(0);
        }
        allHistory.add(history);
    }

    public void setConversingWith(@Nullable ServerPlayerEntity conversingWith) {
        this.conversingWith = conversingWith;
    }

    public @Nullable ServerPlayerEntity getConversingWith() {
        return conversingWith;
    }

    public List<History> getAllHistory() {
        return allHistory;
    }

    public String getRoles() {
        return Role.getRoles(this);
    }

    public List<String> getEmotionalStates() {
        return emotionalStates;
    }

    public GoalSelector getGoalSelector() {
        return goalSelector;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void makeBusy(MobEntity busyWith, @Nullable ServerPlayerEntity announceTo) {
        isBusy = true;
        ((ILivingEntity) busyWith).setDealingWith((MobEntity)(Object)this);
        this.announceTo = announceTo;
    }

    public void freeBusy(boolean force) {
        isBusy = false;
        MobEntity mob = (MobEntity)(Object)this;
        mob.setTarget(null);
        if (announceTo != null && mob.isAlive() && !force) {
            AwakeApiImpl.getResponse(announceTo, "You have completed a task. You will by default return to the player using the function NONE. Do NOT use any other functions. You may let them know you're finished", false);
        }
        this.announceTo = null;
    }
}
