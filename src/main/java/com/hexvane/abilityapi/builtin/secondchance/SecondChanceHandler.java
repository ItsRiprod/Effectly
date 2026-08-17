package com.hexvane.abilityapi.builtin.secondchance;

import com.hexvane.abilityapi.core.AbilityContext;
import com.hexvane.abilityapi.core.AbilityEntry;
import com.hexvane.abilityapi.core.AbilityHandler;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class SecondChanceHandler implements AbilityHandler {

    public static final String ID = "second_chance";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        SecondChanceComponent.register(registry);
        registry.registerSystem(new SecondChanceSystem());
        registry.registerSystem(new SecondChanceCooldownSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        if (context.getComponents().getComponent(context.getRef(), SecondChanceComponent.getComponentType()) == null) {
            context.getComponents().putComponent(context.getRef(), SecondChanceComponent.getComponentType(), new SecondChanceComponent());
        }
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().removeComponent(context.getRef(), SecondChanceComponent.getComponentType());
    }
}
