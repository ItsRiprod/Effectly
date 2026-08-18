package com.riprod.effectly.systems;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.AbilityMutations;

import javax.annotation.Nonnull;

public final class AbilityStatService {

    private AbilityStatService() {}

    public static void applyForPlayer(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world) {
        AbilityMutations.applyAll(ref, store, world);
    }
}
