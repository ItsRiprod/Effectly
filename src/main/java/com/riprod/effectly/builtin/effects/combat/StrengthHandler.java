package com.riprod.effectly.builtin.effects.combat;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.AbilityContext;
import com.riprod.effectly.core.AbilityEntry;
import com.riprod.effectly.core.AbilityHandler;

import javax.annotation.Nonnull;

public final class StrengthHandler implements AbilityHandler {

    public static final String ID = "strength";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        StrengthComponent.register(registry);
        registry.registerSystem(new AbilityStrengthSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        context.getComponents().putComponent(context.getRef(), StrengthComponent.getComponentType(), new StrengthComponent());
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().tryRemoveComponent(context.getRef(), StrengthComponent.getComponentType());
    }
}
