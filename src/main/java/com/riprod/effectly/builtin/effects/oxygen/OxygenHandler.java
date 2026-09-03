package com.riprod.effectly.builtin.effects.oxygen;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.registry.AbilityContext;
import com.riprod.effectly.core.effects.registry.AbilityHandler;
import com.riprod.effectly.core.utils.AbilityConditionUtils;

import javax.annotation.Nonnull;

public final class OxygenHandler implements AbilityHandler {

    public static final String ID = "oxygen";

    private static final String MODIFIER_KEY = "Effectly:oxygen";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<OxygenConfig> getConfigBinding() {
        return ConfigBinding.of(OxygenConfig.class, OxygenConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        OxygenComponent.register(registry);
        registry.registerSystem(new OxygenSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        OxygenComponent component = context.getComponents()
                .getComponent(context.getRef(), OxygenComponent.getComponentType());
        if (component == null) component = new OxygenComponent();
        component.bind(abilityId);
        component.requestRecheck();
        context.getComponents().putComponent(context.getRef(), OxygenComponent.getComponentType(), component);
        apply(context, component);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        OxygenComponent component = context.getComponents()
                .getComponent(context.getRef(), OxygenComponent.getComponentType());
        if (component != null) write(context, component, 0f);
        context.getComponents().tryRemoveComponent(context.getRef(), OxygenComponent.getComponentType());
    }

    @Override
    public void reconcile(@Nonnull AbilityContext context) {
        clearModifier(context);
    }

    private static void clearModifier(@Nonnull AbilityContext context) {
        EntityStatMap statMap = context.getComponents()
                .getComponent(context.getRef(), EntityStatMap.getComponentType());
        if (statMap == null) return;

        int statIndex = DefaultEntityStatTypes.getOxygen();
        if (statIndex < 0 || statIndex >= statMap.size()) return;

        statMap.removeModifier(EntityStatMap.Predictable.SELF, statIndex, MODIFIER_KEY);
    }

    static void apply(@Nonnull AbilityContext context, @Nonnull OxygenComponent component) {
        write(context, component, resolveAmount(context, component));
    }

    private static float resolveAmount(@Nonnull AbilityContext context, @Nonnull OxygenComponent component) {
        String abilityId = component.getAbilityId();
        if (abilityId == null) return 0f;

        double value = AbilityConditionUtils.activeValue(
                context.getRef(), context.getComponents(), context.getWorld(), context.getUuid(), abilityId);
        if (!AbilityConditionUtils.isActive(value) || value <= 0) return 0f;

        return (float) (value
                * component.configOrDefault(OxygenConfig.class, OxygenConfig.DEFAULTS).getUnitsPerSecond());
    }

    private static void write(
            @Nonnull AbilityContext context,
            @Nonnull OxygenComponent component,
            float amount) {
        if (amount == component.getAppliedAmount()) return;

        EntityStatMap statMap = context.getComponents()
                .getComponent(context.getRef(), EntityStatMap.getComponentType());
        if (statMap == null) return;

        int statIndex = DefaultEntityStatTypes.getOxygen();
        if (statIndex < 0 || statIndex >= statMap.size()) return;

        if (amount > 0f) {
            statMap.putModifier(EntityStatMap.Predictable.SELF, statIndex, MODIFIER_KEY,
                    new StaticModifier(Modifier.ModifierTarget.MAX,
                            StaticModifier.CalculationType.ADDITIVE, amount));
        } else {
            statMap.removeModifier(EntityStatMap.Predictable.SELF, statIndex, MODIFIER_KEY);
        }
        component.setAppliedAmount(amount);
    }
}
