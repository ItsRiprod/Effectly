package com.hexvane.abilityapi.builtin.healthregen;

import com.hexvane.abilityapi.core.AbilityContext;
import com.hexvane.abilityapi.core.AbilityEntry;
import com.hexvane.abilityapi.core.AbilityHandler;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class HealthRegenHandler implements AbilityHandler {

    public static final String ID = "health_regen";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        HealthRegenComponent.register(registry);
        registry.registerSystem(new ConditionalHealthRegenSystem());
        registry.registerSystem(new HealthRegenDelayRecordSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        if (context.getComponents().getComponent(context.getRef(), HealthRegenComponent.getComponentType()) == null) {
            context.getComponents().putComponent(context.getRef(), HealthRegenComponent.getComponentType(), new HealthRegenComponent());
        }
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().removeComponent(context.getRef(), HealthRegenComponent.getComponentType());
    }
}
