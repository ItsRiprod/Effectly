package com.riprod.effectly.builtin.effects.combat;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.registry.AbilityContext;
import com.riprod.effectly.core.abilities.registry.AbilityHandlerRegistry;
import com.riprod.effectly.core.effects.registry.AbilityHandler;

import javax.annotation.Nonnull;

public final class ResistanceHandler implements AbilityHandler {

    public static final String ID = "resistance";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<ResistanceConfig> getConfigBinding() {
        return ConfigBinding.of(ResistanceConfig.class, ResistanceConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        ResistanceComponent.register(registry);
        registry.registerSystem(new AbilityDamageResistanceSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        context.getComponents().putComponent(context.getRef(), ResistanceComponent.getComponentType(), new ResistanceComponent());
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        if (AbilityHandlerRegistry.holdsAnyFor(context, this)) return;
        context.getComponents().tryRemoveComponent(context.getRef(), ResistanceComponent.getComponentType());
    }
}
