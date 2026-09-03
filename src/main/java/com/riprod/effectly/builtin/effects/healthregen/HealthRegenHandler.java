package com.riprod.effectly.builtin.effects.healthregen;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.registry.AbilityContext;
import com.riprod.effectly.core.effects.registry.AbilityHandler;

import javax.annotation.Nonnull;

public final class HealthRegenHandler implements AbilityHandler {

    public static final String ID = "health_regen";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<HealthRegenConfig> getConfigBinding() {
        return ConfigBinding.of(HealthRegenConfig.class, HealthRegenConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        HealthRegenComponent.register(registry);
        registry.registerSystem(new ConditionalHealthRegenSystem());
        registry.registerSystem(new HealthRegenDelayRecordSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        HealthRegenComponent component = context.getComponents().getComponent(context.getRef(), HealthRegenComponent.getComponentType());
        if (component == null) component = new HealthRegenComponent();
        component.bind(abilityId);
        context.getComponents().putComponent(context.getRef(), HealthRegenComponent.getComponentType(), component);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().tryRemoveComponent(context.getRef(), HealthRegenComponent.getComponentType());
    }
}
