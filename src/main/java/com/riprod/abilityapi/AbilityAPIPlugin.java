package com.riprod.abilityapi;

import com.riprod.abilityapi.commands.AbilityCommand;
import com.riprod.abilityapi.builtin.combat.PunchDamageHandler;
import com.riprod.abilityapi.builtin.combat.ResistanceHandler;
import com.riprod.abilityapi.builtin.combat.StrengthHandler;
import com.riprod.abilityapi.builtin.darkvision.DarkVisionHandler;
import com.riprod.abilityapi.builtin.mining.MiningFortuneHandler;
import com.riprod.abilityapi.builtin.mining.MiningHasteHandler;
import com.riprod.abilityapi.builtin.healthregen.HealthRegenHandler;
import com.riprod.abilityapi.builtin.itemmagnet.ItemMagnetHandler;
import com.riprod.abilityapi.builtin.secondchance.SecondChanceHandler;
import com.riprod.abilityapi.builtin.staminaregen.StaminaRegenHandler;
import com.riprod.abilityapi.builtin.movespeed.MoveSpeedHandler;
import com.riprod.abilityapi.builtin.oxygen.OxygenHandler;
import com.riprod.abilityapi.builtin.swimspeed.SwimSpeedHandler;
import com.riprod.abilityapi.core.stat.AbilityStatSystem;
import com.riprod.abilityapi.core.stat.StatContributions;
import com.riprod.abilityapi.builtin.wallclimb.WallClimbHandler;
import com.riprod.abilityapi.builtin.survival.FallDamageImmunityHandler;
import com.riprod.abilityapi.builtin.survival.InvulnerabilityHandler;
import com.riprod.abilityapi.builtin.survival.WaterbreathingHandler;
import com.riprod.abilityapi.builtin.flight.FlightHandler;
import com.riprod.abilityapi.ability.AbilityConditionSpec;
import com.riprod.abilityapi.builtin.conditions.HealthCondition;
import com.riprod.abilityapi.builtin.conditions.InSunlightCondition;
import com.riprod.abilityapi.builtin.conditions.InZoneCondition;
import com.riprod.abilityapi.core.AbilityHandlerRegistry;
import com.riprod.abilityapi.core.condition.AbilityConditions;
import com.riprod.abilityapi.core.AbilityRoster;
import com.riprod.abilityapi.core.equipment.EquipmentAbilityComponent;
import com.riprod.abilityapi.core.equipment.EquipmentActiveSlotSystem;
import com.riprod.abilityapi.core.equipment.EquipmentAttachSystem;
import com.riprod.abilityapi.core.equipment.EquipmentChangeSystem;
import com.riprod.abilityapi.core.asset.AbilityAsset;
import com.riprod.abilityapi.core.asset.DefaultAbilityHandlerConfig;
import com.riprod.abilityapi.core.system.AbilityLoginSystem;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.riprod.abilityapi.config.AbilityApiConfig;
import com.riprod.configly.Configly;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

/**
 * AbilityAPI - Library mod for player abilities.
 */
public class AbilityAPIPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public AbilityAPIPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("AbilityAPI v%s loaded", this.getManifest().getVersion());
    }

    @Override
    protected void setup() {
        AbilityHandlerRegistry.reset();
        AbilityConditions.reset();
        StatContributions.reset();

        AbilityConditions.register(new InZoneCondition());
        AbilityConditions.register(new InSunlightCondition());
        AbilityConditions.register(new HealthCondition(AbilityConditionSpec.TYPE_HEALTH_BELOW, false, true));
        AbilityConditions.register(new HealthCondition(AbilityConditionSpec.TYPE_HEALTH_ABOVE, false, false));
        AbilityConditions.register(new HealthCondition(AbilityConditionSpec.TYPE_TARGET_HEALTH_BELOW, true, true));
        AbilityConditions.register(new HealthCondition(AbilityConditionSpec.TYPE_TARGET_HEALTH_ABOVE, true, false));

        Configly.register(AbilityApiConfig.TYPE, AbilityApiConfig.class, AbilityApiConfig.CODEC);

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

        // every contributor must be registered before the stat system materialises its OR query
        this.getEntityStoreRegistry().registerSystem(new AbilityStatSystem());

        this.getEntityStoreRegistry().registerSystem(new AbilityLoginSystem());

        EquipmentAbilityComponent.register(this.getEntityStoreRegistry());
        this.getEntityStoreRegistry().registerSystem(new EquipmentAttachSystem());
        this.getEntityStoreRegistry().registerSystem(new EquipmentChangeSystem());
        this.getEntityStoreRegistry().registerSystem(new EquipmentActiveSlotSystem());

        this.getCommandRegistry().registerCommand(new AbilityCommand(this));
        LOGGER.atInfo().log("AbilityAPI setup complete");
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("AbilityAPI shutdown");
    }
}
