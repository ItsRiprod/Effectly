package com.hexvane.abilityapi.builtin.staminaregen;

import com.hexvane.abilityapi.core.AbilityContext;
import com.hexvane.abilityapi.core.AbilityEntry;
import com.hexvane.abilityapi.core.AbilityHandler;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class StaminaRegenHandler implements AbilityHandler {

    public static final String ID = "stamina_regen";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        StaminaRegenComponent.register(registry);
        registry.registerSystem(new ConditionalStatSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        if (context.getComponents().getComponent(context.getRef(), StaminaRegenComponent.getComponentType()) == null) {
            context.getComponents().putComponent(context.getRef(), StaminaRegenComponent.getComponentType(), new StaminaRegenComponent());
        }
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().removeComponent(context.getRef(), StaminaRegenComponent.getComponentType());
    }
}
