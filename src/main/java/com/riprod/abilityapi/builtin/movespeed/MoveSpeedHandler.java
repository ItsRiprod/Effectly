package com.riprod.abilityapi.builtin.movespeed;

import com.riprod.abilityapi.core.AbilityContext;
import com.riprod.abilityapi.core.AbilityEntry;
import com.riprod.abilityapi.core.AbilityHandler;
import com.riprod.abilityapi.core.stat.StatContributions;
import com.riprod.abilityapi.core.stat.StatPacingComponent;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class MoveSpeedHandler implements AbilityHandler {

    public static final String ID = "move_speed";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        MoveSpeedComponent.register(registry);
        StatPacingComponent.register(registry);
        StatContributions.register(new MoveSpeedContributor());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        context.getComponents().putComponent(context.getRef(), MoveSpeedComponent.getComponentType(), new MoveSpeedComponent());
        StatContributions.onGrant(context);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().removeComponent(context.getRef(), MoveSpeedComponent.getComponentType());
        StatContributions.onRevoke(context);
    }
}
