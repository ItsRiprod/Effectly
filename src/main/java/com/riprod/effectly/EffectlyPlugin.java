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
import com.riprod.effectly.builtin.effects.movement.MovementStateHandler;
import com.riprod.effectly.builtin.effects.oxygen.OxygenHandler;
import com.riprod.effectly.builtin.effects.staminaregen.StaminaRegenHandler;
import com.riprod.effectly.builtin.effects.survival.WaterbreathingHandler;
import com.riprod.effectly.builtin.effects.wallclimb.WallClimbHandler;
import com.riprod.effectly.builtin.actions.fall.BounceAction;
import com.riprod.effectly.builtin.actions.fall.BurstAction;
import com.riprod.effectly.builtin.actions.fall.GustAction;
import com.riprod.effectly.builtin.actions.fall.VibraniumAction;
import com.riprod.effectly.builtin.actions.block.FortuneAction;
import com.riprod.effectly.builtin.actions.block.ModifyBlockDamageAction;
import com.riprod.effectly.builtin.actions.damage.ModifyDamageAction;
import com.riprod.effectly.builtin.actions.damage.ReflectAction;
import com.riprod.effectly.builtin.actions.damage.SecondChanceAction;
import com.riprod.effectly.builtin.conditions.hand.EmptyHandCondition;
import com.riprod.effectly.builtin.capabilities.BlockDamageCapability;
import com.riprod.effectly.builtin.capabilities.BreathCapability;
import com.riprod.effectly.builtin.capabilities.ItemCapability;
import com.riprod.effectly.builtin.triggers.OnAttackProducerSystem;
import com.riprod.effectly.builtin.triggers.OnBreakProducerSystem;
import com.riprod.effectly.builtin.triggers.OnBreathingProducerSystem;
import com.riprod.effectly.builtin.triggers.OnCraftProducerSystem;
import com.riprod.effectly.builtin.triggers.OnDamageBlockProducerSystem;
import com.riprod.effectly.builtin.triggers.OnDamageFilterProducerSystem;
import com.riprod.effectly.builtin.triggers.OnDamagedProducerSystem;
import com.riprod.effectly.builtin.triggers.OnDeathProducerSystem;
import com.riprod.effectly.builtin.triggers.OnDropProducerSystem;
import com.riprod.effectly.builtin.triggers.OnLandProducerSystem;
import com.riprod.effectly.builtin.triggers.OnPickupProducerSystem;
import com.riprod.effectly.builtin.triggers.OnPlaceProducerSystem;
import com.riprod.effectly.builtin.triggers.OnRespawnProducerSystem;
import com.riprod.effectly.builtin.triggers.OnUseBlockProducerSystem;
import com.riprod.effectly.builtin.triggers.OnUseEntityProducerSystem;
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
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorRequestDataSetEvent;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.util.function.Consumer;

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
        CapabilityRegistry.register(BlockDamageCapability.TYPE);
        CapabilityRegistry.register(ItemCapability.TYPE);
        CapabilityRegistry.register(BreathCapability.TYPE);

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
        ActionRegistry.register(VibraniumAction.ID, VibraniumAction.class, VibraniumAction.CODEC);
        ActionRegistry.register(ModifyDamageAction.ID, ModifyDamageAction.class, ModifyDamageAction.CODEC);
        ActionRegistry.register(ReflectAction.ID, ReflectAction.class, ReflectAction.CODEC);
        ActionRegistry.register(SecondChanceAction.ID, SecondChanceAction.class, SecondChanceAction.CODEC);
        ActionRegistry.register(FortuneAction.ID, FortuneAction.class, FortuneAction.CODEC);
        ActionRegistry.register(ModifyBlockDamageAction.ID, ModifyBlockDamageAction.class, ModifyBlockDamageAction.CODEC);

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
        TriggerRegistry.register(new Trigger(OnBreakProducerSystem.TRIGGER,
                OnBreakProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnBreakProducerSystem())));
        TriggerRegistry.register(new Trigger(OnDamageBlockProducerSystem.TRIGGER,
                OnDamageBlockProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnDamageBlockProducerSystem())));
        TriggerRegistry.register(new Trigger(OnPlaceProducerSystem.TRIGGER,
                OnPlaceProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnPlaceProducerSystem())));
        TriggerRegistry.register(new Trigger(OnUseBlockProducerSystem.TRIGGER,
                OnUseBlockProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnUseBlockProducerSystem())));
        TriggerRegistry.register(new Trigger(OnUseEntityProducerSystem.TRIGGER,
                OnUseEntityProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnUseEntityProducerSystem())));
        TriggerRegistry.register(new Trigger(OnPickupProducerSystem.TRIGGER,
                OnPickupProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnPickupProducerSystem())));
        TriggerRegistry.register(new Trigger(OnDropProducerSystem.TRIGGER,
                OnDropProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnDropProducerSystem())));
        TriggerRegistry.register(new Trigger(OnCraftProducerSystem.TRIGGER,
                OnCraftProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnCraftProducerSystem())));
        TriggerRegistry.register(new Trigger(OnRespawnProducerSystem.TRIGGER,
                OnRespawnProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnRespawnProducerSystem())));
        TriggerRegistry.register(new Trigger(OnBreathingProducerSystem.TRIGGER_SUBMERGE,
                OnBreathingProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnBreathingProducerSystem())));
        TriggerRegistry.register(new Trigger(OnBreathingProducerSystem.TRIGGER_ENTER_FLUID,
                OnBreathingProducerSystem.PROVIDES, null));
        TriggerRegistry.register(new Trigger(OnBreathingProducerSystem.TRIGGER_EXIT_FLUID,
                OnBreathingProducerSystem.PROVIDES, null));
        TriggerRegistry.register(new Trigger(OnDeathProducerSystem.TRIGGER,
                OnDeathProducerSystem.PROVIDES,
                registry -> registry.registerSystem(new OnDeathProducerSystem())));
        TriggerRegistry.register(new Trigger(OnDeathProducerSystem.TRIGGER_KILLED,
                OnDeathProducerSystem.PROVIDES_WITH_OTHER, null));
        TriggerRegistry.register(new Trigger(OnDeathProducerSystem.TRIGGER_KILL,
                OnDeathProducerSystem.PROVIDES_WITH_OTHER, null));

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

        var events = this.getEventRegistry();
        events.register(AssetEditorRequestDataSetEvent.class, "EffectlyHandlers",
                (Consumer<AssetEditorRequestDataSetEvent>) e ->
                        e.setResults(AbilityHandlerRegistry.ids().toArray(String[]::new)));
        events.register(AssetEditorRequestDataSetEvent.class, "EffectlyConditions",
                (Consumer<AssetEditorRequestDataSetEvent>) e ->
                        e.setResults(ConditionRegistry.ids().toArray(String[]::new)));

        LOGGER.atInfo().log("Effectly setup complete");
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Effectly shutdown");
    }
}
