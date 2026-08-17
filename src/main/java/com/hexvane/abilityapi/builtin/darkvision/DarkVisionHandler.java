package com.hexvane.abilityapi.builtin.darkvision;

import com.hexvane.abilityapi.core.AbilityContext;
import com.hexvane.abilityapi.core.AbilityEntry;
import com.hexvane.abilityapi.core.AbilityHandler;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class DarkVisionHandler implements AbilityHandler {

    public static final String ID = "dark_vision";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        DarkVisionComponent.register(registry);
        registry.registerSystem(new DarkVisionEffectSystem());
        registry.registerSystem(new DarkVisionCleanupSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        DarkVisionComponent state = context.getComponents().getComponent(context.getRef(), DarkVisionComponent.getComponentType());
        if (state == null) {
            context.getComponents().putComponent(context.getRef(), DarkVisionComponent.getComponentType(), new DarkVisionComponent());
            return;
        }
        state.requestApply();
    }

    @Override
    public void reconcile(@Nonnull AbilityContext context) {
        DarkVisionEffects.remove(context.getRef(), context.getComponents());
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().removeComponent(context.getRef(), DarkVisionComponent.getComponentType());
    }
}
