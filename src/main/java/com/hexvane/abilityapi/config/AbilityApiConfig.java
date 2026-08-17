package com.hexvane.abilityapi.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.configly.Config;
import com.riprod.configly.Configly;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;

public final class AbilityApiConfig extends Config {

    @Nonnull
    public static final String TYPE = "AbilityAPI";

    @Nonnull
    public static final AbilityApiConfig DEFAULTS = new AbilityApiConfig();

    @Nonnull
    public static final BuilderCodec<AbilityApiConfig> CODEC = BuilderCodec
            .builder(AbilityApiConfig.class, AbilityApiConfig::new)
            .append(new KeyedCodec<>("MiningFortuneBlocks", new SetCodec<>(Codec.STRING, LinkedHashSet::new, false)),
                    (config, v) -> config.miningFortuneBlocks = v,
                    config -> config.miningFortuneBlocks)
            .documentation("Block type ids that mining_fortune grants extra drops for. Any block not "
                    + "listed here is unaffected no matter how high the ability value is.")
            .add()
            .append(new KeyedCodec<>("MiningHastePerLevel", Codec.DOUBLE),
                    (config, v) -> config.miningHastePerLevel = v,
                    config -> config.miningHastePerLevel)
            .documentation("Extra block-breaking damage per mining_haste level. Block damage is "
                    + "multiplied by (1 + this * level), so 0.2 at level 5 is 2x.")
            .addValidator(Validators.min(0.0))
            .add()
            .append(new KeyedCodec<>("HealthRegenDelaySeconds", Codec.FLOAT),
                    (config, v) -> config.healthRegenDelaySeconds = v,
                    config -> config.healthRegenDelaySeconds)
            .documentation("Seconds after taking damage before health_regen resumes, mirroring the "
                    + "base-game stamina regen delay feel.")
            .addValidator(Validators.min(0.0f))
            .add()
            .append(new KeyedCodec<>("OxygenUnitsPerSecond", Codec.FLOAT),
                    (config, v) -> config.oxygenUnitsPerSecond = v,
                    config -> config.oxygenUnitsPerSecond)
            .documentation("Oxygen stat units that one point of the oxygen ability adds to max breath. Oxygen.json "
                    + "drains 3 per 0.5s while suffocating, so 6 units is one second underwater.")
            .addValidator(Validators.min(0.0f))
            .add()
            .append(new KeyedCodec<>("SecondChanceRestorePercent", Codec.FLOAT),
                    (config, v) -> config.secondChanceRestorePercent = v,
                    config -> config.secondChanceRestorePercent)
            .documentation("Fraction of max health a player is restored to when second_chance saves "
                    + "them from lethal damage.")
            .addValidator(Validators.range(0.0f, 1.0f))
            .add()
            .append(new KeyedCodec<>("SecondChanceCooldownSeconds", Codec.FLOAT),
                    (config, v) -> config.secondChanceCooldownSeconds = v,
                    config -> config.secondChanceCooldownSeconds)
            .documentation("Cooldown before second_chance can trigger again for the same player.")
            .addValidator(Validators.min(0.0f))
            .add()
            .append(new KeyedCodec<>("ItemMagnetBaseRange", Codec.DOUBLE),
                    (config, v) -> config.itemMagnetBaseRange = v,
                    config -> config.itemMagnetBaseRange)
            .documentation("Magnet radius in blocks at an item_magnet value of 1.0. Effective radius "
                    + "is this multiplied by the ability value.")
            .addValidator(Validators.min(0.0))
            .add()
            .append(new KeyedCodec<>("ItemMagnetLerpSpeed", Codec.DOUBLE),
                    (config, v) -> config.itemMagnetLerpSpeed = v,
                    config -> config.itemMagnetLerpSpeed)
            .documentation("Blocks per second that a magnetised item travels toward the player.")
            .addValidator(Validators.min(0.0))
            .add()
            .append(new KeyedCodec<>("ItemMagnetPickupThreshold", Codec.DOUBLE),
                    (config, v) -> config.itemMagnetPickupThreshold = v,
                    config -> config.itemMagnetPickupThreshold)
            .documentation("Distance at which the magnet stops pulling so normal pickup takes over.")
            .addValidator(Validators.min(0.0))
            .add()
            .append(new KeyedCodec<>("WallClimbProbeDistance", Codec.DOUBLE),
                    (config, v) -> config.wallClimbProbeDistance = v,
                    config -> config.wallClimbProbeDistance)
            .documentation("How far in front of the player to probe for a solid wall, in blocks.")
            .addValidator(Validators.min(0.0))
            .add()
            .append(new KeyedCodec<>("WallClimbVelocity", Codec.DOUBLE),
                    (config, v) -> config.wallClimbVelocity = v,
                    config -> config.wallClimbVelocity)
            .documentation("Base upward velocity applied while climbing a wall.")
            .add()
            .append(new KeyedCodec<>("WallClimbVelocityUp", Codec.DOUBLE),
                    (config, v) -> config.wallClimbVelocityUp = v,
                    config -> config.wallClimbVelocityUp)
            .documentation("Extra upward velocity added while the jump key is held.")
            .add()
            .append(new KeyedCodec<>("WallClimbVelocityDown", Codec.DOUBLE),
                    (config, v) -> config.wallClimbVelocityDown = v,
                    config -> config.wallClimbVelocityDown)
            .documentation("Velocity subtracted while the crouch key is held, to climb down.")
            .add()
            .append(new KeyedCodec<>("FlightReassertSeconds", Codec.FLOAT),
                    (config, v) -> config.flightReassertSeconds = v,
                    config -> config.flightReassertSeconds)
            .documentation("How often creative_flight re-evaluates. This governs both repair after another system "
                    + "resets movement settings AND how quickly conditional flight reacts to its conditions.")
            .addValidator(Validators.min(0.1f))
            .add()
            .append(new KeyedCodec<>("StatReassertSeconds", Codec.FLOAT),
                    (config, v) -> config.statReassertSeconds = v,
                    config -> config.statReassertSeconds)
            .documentation("How often move_speed/swim_speed re-assert themselves. Seven engine paths "
                    + "reset MovementSettings without notifying (login, model change, mount, dismount, "
                    + "respawn, config reload), so a periodic re-apply is the only way to recover.")
            .addValidator(Validators.min(0.1f))
            .add()
            .append(new KeyedCodec<>("DarkVisionEffectId", Codec.STRING),
                    (config, v) -> config.darkVisionEffectId = v,
                    config -> config.darkVisionEffectId)
            .documentation("EntityEffect asset id applied by dark_vision. Override this to ship your "
                    + "own screen effect without replacing the built-in asset.")
            .addValidator(Validators.nonEmptyString())
            .add()
            .append(new KeyedCodec<>("DarkVisionCheckSeconds", Codec.FLOAT),
                    (config, v) -> config.darkVisionCheckSeconds = v,
                    config -> config.darkVisionCheckSeconds)
            .documentation("How often dark_vision re-evaluates its conditions. The effect asset is Infinite, so this "
                    + "governs responsiveness to conditions, not renewal.")
            .addValidator(Validators.min(0.1f))
            .add()
            .append(new KeyedCodec<>("SunlightMinFactor", Codec.DOUBLE),
                    (config, v) -> config.sunlightMinFactor = v,
                    config -> config.sunlightMinFactor)
            .documentation("Minimum world sunlight factor (0-1) for the in_sunlight condition to count "
                    + "as daytime. Below this it is treated as night.")
            .addValidator(Validators.range(0.0, 1.0))
            .add()
            .append(new KeyedCodec<>("SunlightMinEffective", Codec.INTEGER),
                    (config, v) -> config.sunlightMinEffective = v,
                    config -> config.sunlightMinEffective)
            .documentation("Minimum effective sunlight (skyLight * sunlightFactor, 0-15 scale) for the "
                    + "in_sunlight condition to pass.")
            .addValidator(Validators.range(0, 15))
            .add()
            .build();

    private Set<String> miningFortuneBlocks = defaultMiningFortuneBlocks();
    private double miningHastePerLevel = 0.2;
    private float healthRegenDelaySeconds = 5.0f;
    private float oxygenUnitsPerSecond = 6.0f;
    private float secondChanceRestorePercent = 0.2f;
    private float secondChanceCooldownSeconds = 300.0f;
    private double itemMagnetBaseRange = 8.0;
    private double itemMagnetLerpSpeed = 3.0;
    private double itemMagnetPickupThreshold = 0.5;
    private double wallClimbProbeDistance = 0.6;
    private double wallClimbVelocity = 1.2;
    private double wallClimbVelocityUp = 0.2;
    private double wallClimbVelocityDown = 0.5;
    private float flightReassertSeconds = 15.0f;
    private float statReassertSeconds = 1.0f;
    private String darkVisionEffectId = "hexvane_abilityapi_dark_vision";
    private float darkVisionCheckSeconds = 1.0f;
    private double sunlightMinFactor = 0.2;
    private int sunlightMinEffective = 10;

    private AbilityApiConfig() {
    }

    @Nonnull
    public static AbilityApiConfig get() {
        return Configly.getOrElse(TYPE, AbilityApiConfig.class, DEFAULTS);
    }

    @Nonnull
    public Set<String> getMiningFortuneBlocks() {
        return miningFortuneBlocks;
    }

    public double getMiningHastePerLevel() {
        return miningHastePerLevel;
    }

    public float getHealthRegenDelaySeconds() {
        return healthRegenDelaySeconds;
    }

    public float getOxygenUnitsPerSecond() {
        return oxygenUnitsPerSecond;
    }

    public float getSecondChanceRestorePercent() {
        return secondChanceRestorePercent;
    }

    public float getSecondChanceCooldownSeconds() {
        return secondChanceCooldownSeconds;
    }

    public double getItemMagnetBaseRange() {
        return itemMagnetBaseRange;
    }

    public double getItemMagnetLerpSpeed() {
        return itemMagnetLerpSpeed;
    }

    public double getItemMagnetPickupThreshold() {
        return itemMagnetPickupThreshold;
    }

    public double getWallClimbProbeDistance() {
        return wallClimbProbeDistance;
    }

    public double getWallClimbVelocity() {
        return wallClimbVelocity;
    }

    public double getWallClimbVelocityUp() {
        return wallClimbVelocityUp;
    }

    public double getWallClimbVelocityDown() {
        return wallClimbVelocityDown;
    }

    public float getFlightReassertSeconds() {
        return flightReassertSeconds;
    }

    public float getStatReassertSeconds() {
        return statReassertSeconds;
    }

    @Nonnull
    public String getDarkVisionEffectId() {
        return darkVisionEffectId;
    }

    public float getDarkVisionCheckSeconds() {
        return darkVisionCheckSeconds;
    }

    public double getSunlightMinFactor() {
        return sunlightMinFactor;
    }

    public int getSunlightMinEffective() {
        return sunlightMinEffective;
    }

    @Nonnull
    private static Set<String> defaultMiningFortuneBlocks() {
        return new HashSet<>(Set.of(
                "Ore_Adamantite_Basalt", "Ore_Adamantite_Shale", "Ore_Adamantite_Slate", "Ore_Adamantite_Stone", "Ore_Adamantite_Volcanic",
                "Ore_Cobalt_Basalt", "Ore_Cobalt_Sandstone", "Ore_Cobalt_Shale", "Ore_Cobalt_Slate", "Ore_Cobalt_Stone", "Ore_Cobalt_Volcanic",
                "Ore_Copper_Basalt", "Ore_Copper_Sandstone", "Ore_Copper_Shale", "Ore_Copper_Stone", "Ore_Copper_Volcanic",
                "Ore_Gold_Basalt", "Ore_Gold_Sandstone", "Ore_Gold_Shale", "Ore_Gold_Stone", "Ore_Gold_Volcanic",
                "Ore_Iron_Basalt", "Ore_Iron_Sandstone", "Ore_Iron_Shale", "Ore_Iron_Slate", "Ore_Iron_Stone", "Ore_Iron_Volcanic",
                "Ore_Mithril_Basalt", "Ore_Mithril_Magma", "Ore_Mithril_Slate", "Ore_Mithril_Stone", "Ore_Mithril_Volcanic",
                "Ore_Onyxium_Basalt", "Ore_Onyxium_Sandstone", "Ore_Onyxium_Shale", "Ore_Onyxium_Stone", "Ore_Onyxium_Volcanic",
                "Ore_Silver_Basalt", "Ore_Silver_Sandstone", "Ore_Silver_Shale", "Ore_Silver_Slate", "Ore_Silver_Stone", "Ore_Silver_Volcanic",
                "Ore_Thorium_Basalt", "Ore_Thorium_Sandstone", "Ore_Thorium_Shale", "Ore_Thorium_Stone", "Ore_Thorium_Volcanic", "Ore_Thorium_Mud"));
    }
}
