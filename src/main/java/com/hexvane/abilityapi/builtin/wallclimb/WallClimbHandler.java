package com.hexvane.abilityapi.builtin.wallclimb;

import com.hexvane.abilityapi.core.AbilityContext;
import com.hexvane.abilityapi.core.AbilityEntry;
import com.hexvane.abilityapi.core.AbilityHandler;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class WallClimbHandler implements AbilityHandler {

    public static final String ID = "wall_climb";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        WallClimbComponent.register(registry);
        registry.registerSystem(new WallClimbSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        if (context.getComponents().getComponent(context.getRef(), WallClimbComponent.getComponentType()) == null) {
            context.getComponents().putComponent(context.getRef(), WallClimbComponent.getComponentType(), new WallClimbComponent());
        }
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().removeComponent(context.getRef(), WallClimbComponent.getComponentType());
    }
}
