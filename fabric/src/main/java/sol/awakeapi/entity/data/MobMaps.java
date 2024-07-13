package sol.awakeapi.entity.data;

import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import java.util.HashMap;
import java.util.Map;

public class MobMaps {
    private static final Map<String, Class<? extends MobEntity>> passiveMobs = new HashMap<>();
    private static final Map<String, Class<? extends MobEntity>> neutralMobs = new HashMap<>();
    private static final Map<String, Class<? extends MobEntity>> hostileMobs = new HashMap<>();

    static {
        // passive mobs
        passiveMobs.put("allay", AllayEntity.class);
        passiveMobs.put("axolotl", AxolotlEntity.class);
        passiveMobs.put("bat", BatEntity.class);
        passiveMobs.put("camel", CamelEntity.class);
        passiveMobs.put("cat", CatEntity.class);
        passiveMobs.put("chicken", ChickenEntity.class);
        passiveMobs.put("cod", CodEntity.class);
        passiveMobs.put("cow", CowEntity.class);
        passiveMobs.put("donkey", DonkeyEntity.class);
        passiveMobs.put("frog", FrogEntity.class);
        passiveMobs.put("glowsquid", GlowSquidEntity.class);
        passiveMobs.put("horse", HorseEntity.class);
        passiveMobs.put("mooshroom", MooshroomEntity.class);
        passiveMobs.put("mule", MuleEntity.class);
        passiveMobs.put("ocelot", OcelotEntity.class);
        passiveMobs.put("parrot", ParrotEntity.class);
        passiveMobs.put("pig", PigEntity.class);
        passiveMobs.put("pufferfish", PufferfishEntity.class);
        passiveMobs.put("rabbit", RabbitEntity.class);
        passiveMobs.put("salmon", SalmonEntity.class);
        passiveMobs.put("sheep", SheepEntity.class);
        passiveMobs.put("skeletonhorse", SkeletonHorseEntity.class);
        passiveMobs.put("sniffer", SnifferEntity.class);
        passiveMobs.put("snowgolem", SnowGolemEntity.class);
        passiveMobs.put("squid", SquidEntity.class);
        passiveMobs.put("strider", StriderEntity.class);
        passiveMobs.put("tadpole", TadpoleEntity.class);
        passiveMobs.put("tropicalfish", TropicalFishEntity.class);
        passiveMobs.put("turtle", TurtleEntity.class);
        passiveMobs.put("villager", VillagerEntity.class);
        passiveMobs.put("wanderingtrader", WanderingTraderEntity.class);

        // neutral mobs
        neutralMobs.put("bee", BeeEntity.class);
        neutralMobs.put("cavespider", CaveSpiderEntity.class);
        neutralMobs.put("dolphin", DolphinEntity.class);
        neutralMobs.put("drowned", DrownedEntity.class);
        neutralMobs.put("enderman", EndermanEntity.class);
        neutralMobs.put("fox", FoxEntity.class);
        neutralMobs.put("goat", GoatEntity.class);
        neutralMobs.put("irongolem", IronGolemEntity.class);
        neutralMobs.put("llama", LlamaEntity.class);
        neutralMobs.put("panda", PandaEntity.class);
        neutralMobs.put("piglin", PiglinEntity.class);
        neutralMobs.put("polarbear", PolarBearEntity.class);
        neutralMobs.put("bear", PolarBearEntity.class);
        neutralMobs.put("spider", SpiderEntity.class);
        neutralMobs.put("wolf", WolfEntity.class);
        neutralMobs.put("dog", WolfEntity.class);
        neutralMobs.put("zombiepiglin", ZombifiedPiglinEntity.class);
        neutralMobs.put("zombiepigman", ZombifiedPiglinEntity.class);
        neutralMobs.put("pigman", ZombifiedPiglinEntity.class);

        // hostile mobs
        hostileMobs.put("blaze", BlazeEntity.class);
        hostileMobs.put("creeper", CreeperEntity.class);
        hostileMobs.put("elderguardian", ElderGuardianEntity.class);
        hostileMobs.put("endermite", EndermiteEntity.class);
        hostileMobs.put("enderdragon", EnderDragonEntity.class);
        hostileMobs.put("evoker", EvokerEntity.class);
        hostileMobs.put("ghast", GhastEntity.class);
        hostileMobs.put("guardian", GuardianEntity.class);
        hostileMobs.put("hoglin", HoglinEntity.class);
        hostileMobs.put("husk", HuskEntity.class);
        hostileMobs.put("magmacube", MagmaCubeEntity.class);
        hostileMobs.put("phantom", PhantomEntity.class);
        hostileMobs.put("piglinbrute", PiglinBruteEntity.class);
        hostileMobs.put("pillager", PillagerEntity.class);
        hostileMobs.put("ravager", RavagerEntity.class);
        hostileMobs.put("shulker", ShulkerEntity.class);
        hostileMobs.put("silverfish", SilverfishEntity.class);
        hostileMobs.put("skeleton", SkeletonEntity.class);
        hostileMobs.put("slime", SlimeEntity.class);
        hostileMobs.put("stray", StrayEntity.class);
        hostileMobs.put("vex", VexEntity.class);
        hostileMobs.put("vindicator", VindicatorEntity.class);
        hostileMobs.put("warden", WardenEntity.class);
        hostileMobs.put("witch", WitchEntity.class);
        hostileMobs.put("wither", WitherEntity.class);
        hostileMobs.put("witherskeleton", WitherSkeletonEntity.class);
        hostileMobs.put("zoglin", ZoglinEntity.class);
        hostileMobs.put("zombie", ZombieEntity.class);
        hostileMobs.put("zombievillager", ZombieVillagerEntity.class);
    }

    public static Class<? extends MobEntity> getMobClass(String mobName) {
        mobName = mobName.toLowerCase().replace("_", "").replace(" ", "");
        // Check passive mobs
        if (passiveMobs.containsKey(mobName)) {
            return passiveMobs.get(mobName);
        }

        // Check neutral mobs
        if (neutralMobs.containsKey(mobName)) {
            return neutralMobs.get(mobName);
        }

        // Check hostile mobs
        if (hostileMobs.containsKey(mobName)) {
            return hostileMobs.get(mobName);
        }

        // Return null if mob not found
        return null;
    }
}

