package com.hexvane.abilityapi.builtin.oxygen;

import com.hexvane.abilityapi.core.AbilityContext;
import com.hexvane.abilityapi.core.AbilityEntry;
import com.hexvane.abilityapi.core.AbilityHandler;
import com.hexvane.abilityapi.core.stat.StatContributions;
import com.hexvane.abilityapi.core.stat.StatPacingComponent;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class OxygenHandler implements AbilityHandler {

    public static final String ID = "oxygen";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        OxygenComponent.register(registry);
        StatPacingComponent.register(registry);
        StatContributions.register(new OxygenContributor());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        context.getComponents().putComponent(context.getRef(), OxygenComponent.getComponentType(), new OxygenComponent());
        StatContributions.onGrant(context);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().removeComponent(context.getRef(), OxygenComponent.getComponentType());
        StatContributions.onRevoke(context);
    }
}
