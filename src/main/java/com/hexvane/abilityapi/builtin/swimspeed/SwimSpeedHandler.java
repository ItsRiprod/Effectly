package com.hexvane.abilityapi.builtin.swimspeed;

import com.hexvane.abilityapi.core.AbilityContext;
import com.hexvane.abilityapi.core.AbilityEntry;
import com.hexvane.abilityapi.core.AbilityHandler;
import com.hexvane.abilityapi.core.stat.StatContributions;
import com.hexvane.abilityapi.core.stat.StatPacingComponent;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class SwimSpeedHandler implements AbilityHandler {

    public static final String ID = "swim_speed";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        SwimSpeedComponent.register(registry);
        StatPacingComponent.register(registry);
        StatContributions.register(new SwimSpeedContributor());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        context.getComponents().putComponent(context.getRef(), SwimSpeedComponent.getComponentType(), new SwimSpeedComponent());
        StatContributions.onGrant(context);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().removeComponent(context.getRef(), SwimSpeedComponent.getComponentType());
        StatContributions.onRevoke(context);
    }
}
