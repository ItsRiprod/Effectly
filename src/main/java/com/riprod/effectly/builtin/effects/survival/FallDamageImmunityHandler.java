package com.riprod.effectly.builtin.effects.survival;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.AbilityContext;
import com.riprod.effectly.core.AbilityEntry;
import com.riprod.effectly.core.AbilityHandler;

import javax.annotation.Nonnull;

public final class FallDamageImmunityHandler implements AbilityHandler {

    public static final String ID = "fall_damage_immunity";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        FallDamageImmunityComponent.register(registry);
        registry.registerSystem(new FallDamageImmunitySystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        context.getComponents().putComponent(context.getRef(), FallDamageImmunityComponent.getComponentType(), new FallDamageImmunityComponent());
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().tryRemoveComponent(context.getRef(), FallDamageImmunityComponent.getComponentType());
    }
}
