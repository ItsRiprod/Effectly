package com.hexvane.abilityapi.core.stat;

import com.hexvane.abilityapi.core.AbilityContext;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public interface StatContributor {

    @Nonnull
    ComponentType<EntityStore, ?> componentType();

    void contribute(@Nonnull AbilityContext context, @Nonnull StatAccumulator accumulator);
}
