package com.riprod.effectly;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.riprod.configly.Configly;
import com.riprod.effectly.builtin.conditions.health.HealthCondition;
import com.riprod.effectly.builtin.conditions.liquid.InLiquidCondition;
import com.riprod.effectly.builtin.conditions.skylight.SkyLightCondition;
import com.riprod.effectly.builtin.conditions.zone.ZoneCondition;
import com.riprod.effectly.builtin.effects.combat.PunchDamageHandler;
import com.riprod.effectly.builtin.effects.combat.ResistanceHandler;
import com.riprod.effectly.builtin.effects.combat.StrengthHandler;
import com.riprod.effectly.builtin.effects.darkvision.DarkVisionHandler;
import com.riprod.effectly.builtin.effects.healthregen.HealthRegenHandler;
import com.riprod.effectly.builtin.effects.itemmagnet.ItemMagnetHandler;
import com.riprod.effectly.builtin.effects.mining.MiningFortuneHandler;
import com.riprod.effectly.builtin.effects.mining.MiningHasteHandler;
import com.riprod.effectly.builtin.effects.movement.MovementStateHandler;
import com.riprod.effectly.builtin.effects.oxygen.OxygenHandler;
import com.riprod.effectly.builtin.effects.secondchance.SecondChanceHandler;
import com.riprod.effectly.builtin.effects.staminaregen.StaminaRegenHandler;
import com.riprod.effectly.builtin.effects.survival.FallDamageImmunityHandler;
import com.riprod.effectly.builtin.effects.survival.InvulnerabilityHandler;
import com.riprod.effectly.builtin.effects.survival.WaterbreathingHandler;
import com.riprod.effectly.builtin.effects.wallclimb.WallClimbHandler;
import com.riprod.effectly.commands.AbilityCommand;
import com.riprod.effectly.config.EffectlyConfig;
import com.riprod.effectly.core.abilities.component.AbilityComponent;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.abilities.registry.AbilityHandlerRegistry;
import com.riprod.effectly.core.conditions.registry.ConditionAsset;
import com.riprod.effectly.core.conditions.registry.ConditionRegistry;
import com.riprod.effectly.core.conditions.registry.DefaultConditionConfig;
import com.riprod.effectly.core.effects.registry.EffectAsset;
import com.riprod.effectly.core.effects.registry.DefaultAbilityHandlerConfig;
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
        ConditionRegistry.reset();

        DefaultConditionConfig.register();

        ConditionRegistry.register(new ZoneCondition());
        ConditionRegistry.register(new SkyLightCondition());
        ConditionRegistry.register(new HealthCondition());
        ConditionRegistry.register(new InLiquidCondition());

        Configly.register(EffectlyConfig.TYPE, EffectlyConfig.class, EffectlyConfig.CODEC);

        AssetRegistry.register(HytaleAssetStore
                .builder(ConditionAsset.class, new IndexedLookupTableAssetMap<>(ConditionAsset[]::new))
                .setPath(ConditionAsset.ASSET_PATH)
                .setCodec(ConditionAsset.CODEC)
                .setKeyFunction(ConditionAsset::getId)
                .setReplaceOnRemove(ConditionAsset::getDisabledFor)
                .build());

        AssetRegistry.register(HytaleAssetStore
                .builder(EffectAsset.class, new IndexedLookupTableAssetMap<>(EffectAsset[]::new))
                .setPath(EffectAsset.ASSET_PATH)
                .setCodec(EffectAsset.CODEC)
                .setKeyFunction(EffectAsset::getId)
                .setReplaceOnRemove(EffectAsset::getDisabledFor)
                .loadsAfter(DamageCause.class)
                .build());

        var entityStoreRegistry = this.getEntityStoreRegistry();

        var abilityComponentType = entityStoreRegistry.registerComponent(AbilityComponent.class,
                AbilityComponent.ID, AbilityComponent.CODEC);
        AbilityComponent.setComponentType(abilityComponentType);
        ResolvedAbilityComponent.register(entityStoreRegistry);

        DefaultAbilityHandlerConfig.register();

        AbilityHandlerRegistry.register(new MovementStateHandler());
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
        AbilityHandlerRegistry.installAll(entityStoreRegistry);

        EquipmentAbilityComponent.register(entityStoreRegistry);
        entityStoreRegistry.registerSystem(new AbilityLoginSystem());
        entityStoreRegistry.registerSystem(new EquipmentAttachSystem());
        entityStoreRegistry.registerSystem(new EquipmentChangeSystem());
        // this.getEntityStoreRegistry().registerSystem(new
        // EquipmentActiveSlotSystem());

        this.getCommandRegistry().registerCommand(new AbilityCommand(this));
        LOGGER.atInfo().log("Effectly setup complete");
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Effectly shutdown");
    }
}
