package com.riprod.effectly.core.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.AbilityMutations;
import com.riprod.effectly.core.AbilityRoster;

import javax.annotation.Nonnull;

public final class AbilityLoginSystem extends RefSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return AbilityRoster.getComponentType();
    }

    @Override
    public void onEntityAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull AddReason reason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        World world = store.getExternalData().getWorld();
        if (world == null) return;

        AbilityMutations.applyAll(ref, commandBuffer, world);
    }

    @Override
    public void onEntityRemove(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull RemoveReason reason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    }
}
