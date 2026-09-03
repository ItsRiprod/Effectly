package com.riprod.effectly;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.riprod.configly.Configly;
import com.riprod.effectly.builtin.conditions.health.HealthCondition;
import com.riprod.effectly.builtin.conditions.liquid.InLiquidCondition;
import com.riprod.effectly.builtin.conditions.skylight.SkyLightCondition;
import com.riprod.effectly.builtin.conditions.zone.ZoneCondition;
import com.riprod.effectly.builtin.effects.darkvision.DarkVisionHandler;
import com.riprod.effectly.builtin.effects.healthregen.HealthRegenHandler;
import com.riprod.effectly.builtin.effects.itemmagnet.ItemMagnetHandler;
import com.riprod.effectly.builtin.effects.mining.MiningFortuneHandler;
import com.riprod.effectly.builtin.effects.mining.MiningHasteHandler;
import com.riprod.effectly.builtin.effects.movement.MovementStateHandler;
import com.riprod.effectly.builtin.effects.oxygen.OxygenHandler;
import com.riprod.effectly.builtin.effects.staminaregen.StaminaRegenHandler;
import com.riprod.effectly.builtin.effects.survival.WaterbreathingHandler;
import com.riprod.effectly.builtin.effects.wallclimb.WallClimbHandler;
import com.riprod.effectly.builtin.actions.fall.BounceAction;
import com.riprod.effectly.builtin.actions.fall.BurstAction;
import com.riprod.effectly.builtin.actions.fall.GustAction;
import com.riprod.effectly.builtin.actions.fall.HeavyAction;
import com.riprod.effectly.builtin.actions.fall.VibraniumAction;
import com.riprod.effectly.builtin.actions.damage.ModifyDamageAction;
import com.riprod.effectly.builtin.actions.damage.ReflectAction;
import com.riprod.effectly.builtin.actions.damage.SecondChanceAction;
import com.riprod.effectly.builtin.conditions.hand.EmptyHandCondition;
import com.riprod.effectly.builtin.triggers.OnAttackProducerSystem;
import com.riprod.effectly.builtin.triggers.OnDamageFilterProducerSystem;
import com.riprod.effectly.builtin.triggers.OnDamagedProducerSystem;
import com.riprod.effectly.builtin.triggers.OnLandProducerSystem;
import com.riprod.effectly.builtin.triggers.VictimDamageProducerSystem;
import com.riprod.effectly.commands.AbilityCommand;
import com.riprod.effectly.config.EffectlyConfig;
import com.riprod.effectly.core.abilities.component.AbilityComponent;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.abilities.registry.AbilityHandlerRegistry;
import com.riprod.effectly.builtin.capabilities.DamageCapability;
import com.riprod.effectly.builtin.capabilities.MagnitudeCapability;
import com.riprod.effectly.builtin.capabilities.OtherEntityCapability;
import com.riprod.effectly.builtin.capabilities.PositionCapability;
import com.riprod.effectly.builtin.capabilities.SelfCapability;
import com.riprod.effectly.core.actions.ActionRegistry;
import com.riprod.effectly.core.actions.capability.CapabilityRegistry;
import com.riprod.effectly.core.actions.DefaultAction;
import com.riprod.effectly.core.actions.component.ActionCooldownSystem;
import com.riprod.effectly.core.actions.component.ActionHolderComponent;
import com.riprod.effectly.core.actions.trigger.Trigger;
import com.riprod.effectly.core.actions.trigger.TriggerRegistry;
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
        var entityStoreRegistry = this.getEntityStoreRegistry();
        AbilityHandlerRegistry.reset();
        ConditionRegistry.reset();
        ActionRegistry.reset();
        TriggerRegistry.reset();
        CapabilityRegistry.reset();

        // Capabilities - the contract between a trigger and the actions it fires
        CapabilityRegistry.register(SelfCapability.TYPE);
        CapabilityRegistry.register(PositionCapability.TYPE);
        CapabilityRegistry.register(MagnitudeCapability.TYPE);
        CapabilityRegistry.register(DamageCapability.TYPE);
        CapabilityRegistry.register(OtherEntityCapability.TYPE);

        // Components - registered before any system class is touched, because a producer's
        // static Query field resolves its component type during class initialization
        var abilityComponentType = entityStoreRegistry.registerComponent(AbilityComponent.class,
                AbilityComponent.ID, AbilityComponent.CODEC);
        AbilityComponent.setComponentType(abilityComponentType);
        ResolvedAbilityComponent.register(entityStoreRegistry);
        ActionHolderComponent.register(entityStoreRegistry);

        // Conditions - gates for effects
        DefaultConditionConfig.register();

        ConditionRegistry.register(new ZoneCondition());
        ConditionRegistry.register(new SkyLightCondition());
        ConditionRegistry.register(new HealthCondition());
        ConditionRegistry.register(new InLiquidCondition());
        ConditionRegistry.register(new EmptyHandCondition());

        // Actions - stuff that do things
        DefaultAction.register();
        ActionRegistry.register(BounceAction.ID, BounceAction.class, BounceAction.CODEC);
        ActionRegistry.register(GustAction.ID, GustAction.class, GustAction.CODEC);
        ActionRegistry.register(BurstAction.ID, BurstAction.class, BurstAction.CODEC);
        ActionRegistry.register(HeavyAction.ID, HeavyAction.class, HeavyAction.CODEC);
        ActionRegistry.register(VibraniumAction.ID, VibraniumAction.class, VibraniumAction.CODEC);
        ActionRegistry.register(ModifyDamageAction.ID, ModifyDamageAction.class, ModifyDamageAction.CODEC);
        ActionRegistry.register(ReflectAction.ID, ReflectAction.class, ReflectAction.CODEC);
        ActionRegistry.register(SecondChanceAction.ID, SecondChanceAction.class, SecondChanceAction.CODEC);

        // Triggers - things that invoke the Actions
        TriggerRegistry.register(new Trigger(OnLandProducerSystem.TRIGGER,
                OnLandProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnLandProducerSystem())));
        TriggerRegistry.register(new Trigger(OnDamageFilterProducerSystem.TRIGGER,
                VictimDamageProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnDamageFilterProducerSystem())));
        TriggerRegistry.register(new Trigger(OnDamagedProducerSystem.TRIGGER,
                VictimDamageProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnDamagedProducerSystem())));
        TriggerRegistry.register(new Trigger(OnDamagedProducerSystem.TRIGGER_ATTACKED,
                VictimDamageProducerSystem.PROVIDES_WITH_ATTACKER, null));
        TriggerRegistry.register(new Trigger(OnDamagedProducerSystem.TRIGGER_FALL,
                VictimDamageProducerSystem.PROVIDES, null));
        TriggerRegistry.register(new Trigger(OnAttackProducerSystem.TRIGGER,
                OnAttackProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnAttackProducerSystem())));

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

        DefaultAbilityHandlerConfig.register();

        AbilityHandlerRegistry.register(new MovementStateHandler());
        AbilityHandlerRegistry.register(new DarkVisionHandler());
        AbilityHandlerRegistry.register(new WaterbreathingHandler());
        AbilityHandlerRegistry.register(new MiningHasteHandler());
        AbilityHandlerRegistry.register(new MiningFortuneHandler());
        AbilityHandlerRegistry.register(new WallClimbHandler());
        AbilityHandlerRegistry.register(new StaminaRegenHandler());
        AbilityHandlerRegistry.register(new HealthRegenHandler());
        AbilityHandlerRegistry.register(new ItemMagnetHandler());
        AbilityHandlerRegistry.register(new OxygenHandler());
        AbilityHandlerRegistry.installAll(entityStoreRegistry);
        TriggerRegistry.installAll(entityStoreRegistry);
        entityStoreRegistry.registerSystem(new ActionCooldownSystem());

        EquipmentAbilityComponent.register(entityStoreRegistry);
        entityStoreRegistry.registerSystem(new AbilityLoginSystem());
        entityStoreRegistry.registerSystem(new EquipmentAttachSystem());
        entityStoreRegistry.registerSystem(new EquipmentChangeSystem());
        // this.getEntityStoreRegistry().registerSystem(new
        // EquipmentActiveSlotSystem());

        // commands
        this.getCommandRegistry().registerCommand(new AbilityCommand(this));
        LOGGER.atInfo().log("Effectly setup complete");
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Effectly shutdown");
    }
}
