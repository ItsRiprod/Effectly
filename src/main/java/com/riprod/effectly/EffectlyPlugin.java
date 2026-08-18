package com.riprod.effectly;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.riprod.configly.Configly;
import com.riprod.effectly.ability.AbilityConditionSpec;
import com.riprod.effectly.builtin.conditions.HealthCondition;
import com.riprod.effectly.builtin.conditions.InSunlightCondition;
import com.riprod.effectly.builtin.conditions.InZoneCondition;
import com.riprod.effectly.builtin.effects.combat.PunchDamageHandler;
import com.riprod.effectly.builtin.effects.combat.ResistanceHandler;
import com.riprod.effectly.builtin.effects.combat.StrengthHandler;
import com.riprod.effectly.builtin.effects.darkvision.DarkVisionHandler;
import com.riprod.effectly.builtin.effects.flight.FlightHandler;
import com.riprod.effectly.builtin.effects.healthregen.HealthRegenHandler;
import com.riprod.effectly.builtin.effects.itemmagnet.ItemMagnetHandler;
import com.riprod.effectly.builtin.effects.mining.MiningFortuneHandler;
import com.riprod.effectly.builtin.effects.mining.MiningHasteHandler;
import com.riprod.effectly.builtin.effects.movespeed.MoveSpeedHandler;
import com.riprod.effectly.builtin.effects.oxygen.OxygenHandler;
import com.riprod.effectly.builtin.effects.secondchance.SecondChanceHandler;
import com.riprod.effectly.builtin.effects.staminaregen.StaminaRegenHandler;
import com.riprod.effectly.builtin.effects.survival.FallDamageImmunityHandler;
import com.riprod.effectly.builtin.effects.survival.InvulnerabilityHandler;
import com.riprod.effectly.builtin.effects.survival.WaterbreathingHandler;
import com.riprod.effectly.builtin.effects.swimspeed.SwimSpeedHandler;
import com.riprod.effectly.builtin.effects.wallclimb.WallClimbHandler;
import com.riprod.effectly.commands.AbilityCommand;
import com.riprod.effectly.config.EffectlyConfig;
import com.riprod.effectly.core.AbilityHandlerRegistry;
import com.riprod.effectly.core.AbilityRoster;
import com.riprod.effectly.core.asset.AbilityAsset;
import com.riprod.effectly.core.asset.DefaultAbilityHandlerConfig;
import com.riprod.effectly.core.condition.AbilityConditions;
import com.riprod.effectly.core.equipment.EquipmentAbilityComponent;
import com.riprod.effectly.core.equipment.EquipmentAttachSystem;
import com.riprod.effectly.core.equipment.EquipmentChangeSystem;
import com.riprod.effectly.core.system.AbilityLoginSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

/**
 * Effectly - Library mod for player abilities.
 */
public class EffectlyPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public EffectlyPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Effectly v%s loaded", this.getManifest().getVersion());
    }

    @Override
    protected void setup() {
        AbilityHandlerRegistry.reset();
        AbilityConditions.reset();

        AbilityConditions.register(new InZoneCondition());
        AbilityConditions.register(new InSunlightCondition());
        AbilityConditions.register(new HealthCondition(AbilityConditionSpec.TYPE_HEALTH_BELOW, false, true));
        AbilityConditions.register(new HealthCondition(AbilityConditionSpec.TYPE_HEALTH_ABOVE, false, false));
        AbilityConditions.register(new HealthCondition(AbilityConditionSpec.TYPE_TARGET_HEALTH_BELOW, true, true));
        AbilityConditions.register(new HealthCondition(AbilityConditionSpec.TYPE_TARGET_HEALTH_ABOVE, true, false));

        Configly.register(EffectlyConfig.TYPE, EffectlyConfig.class, EffectlyConfig.CODEC);

        AssetRegistry.register(HytaleAssetStore
                .builder(AbilityAsset.class, new DefaultAssetMap<String, AbilityAsset>())
                .setPath(AbilityAsset.ASSET_PATH)
                .setCodec(AbilityAsset.CODEC)
                .setKeyFunction(AbilityAsset::getId)
                .loadsAfter(DamageCause.class)
                .build());

        AbilityRoster.register(this.getEntityStoreRegistry());

        DefaultAbilityHandlerConfig.register();

        AbilityHandlerRegistry.register(new FlightHandler());
        AbilityHandlerRegistry.register(new DarkVisionHandler());
        AbilityHandlerRegistry.register(new ResistanceHandler());
        AbilityHandlerRegistry.register(new StrengthHandler());
        AbilityHandlerRegistry.register(new PunchDamageHandler());
        AbilityHandlerRegistry.register(new FallDamageImmunityHandler());
        AbilityHandlerRegistry.register(new InvulnerabilityHandler());
        AbilityHandlerRegistry.register(new WaterbreathingHandler());
        AbilityHandlerRegistry.register(new MiningHasteHandler());
        AbilityHandlerRegistry.register(new MiningFortuneHandler());
        AbilityHandlerRegistry.register(new WallClimbHandler());
        AbilityHandlerRegistry.register(new StaminaRegenHandler());
        AbilityHandlerRegistry.register(new HealthRegenHandler());
        AbilityHandlerRegistry.register(new SecondChanceHandler());
        AbilityHandlerRegistry.register(new ItemMagnetHandler());
        AbilityHandlerRegistry.register(new OxygenHandler());
        AbilityHandlerRegistry.register(new MoveSpeedHandler());
        AbilityHandlerRegistry.register(new SwimSpeedHandler());
        AbilityHandlerRegistry.installAll(this.getEntityStoreRegistry());

        this.getEntityStoreRegistry().registerSystem(new AbilityLoginSystem());

        EquipmentAbilityComponent.register(this.getEntityStoreRegistry());
        this.getEntityStoreRegistry().registerSystem(new EquipmentAttachSystem());
        this.getEntityStoreRegistry().registerSystem(new EquipmentChangeSystem());
        // this.getEntityStoreRegistry().registerSystem(new EquipmentActiveSlotSystem());

        this.getCommandRegistry().registerCommand(new AbilityCommand(this));
        LOGGER.atInfo().log("Effectly setup complete");
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Effectly shutdown");
    }
}
