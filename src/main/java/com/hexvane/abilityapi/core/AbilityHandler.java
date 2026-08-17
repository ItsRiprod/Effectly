package com.hexvane.abilityapi.core;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public interface AbilityHandler {

    @Nonnull
    String getId();

    default void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
    }

    default void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
    }

    default void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
    }

    default void reconcile(@Nonnull AbilityContext context) {
    }
}
