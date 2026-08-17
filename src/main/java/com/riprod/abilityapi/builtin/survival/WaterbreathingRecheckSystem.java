package com.riprod.abilityapi.builtin.survival;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.modules.entity.component.BreathingComponent;
import com.hypixel.hytale.server.core.modules.entity.component.CachedStatsComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class WaterbreathingRecheckSystem extends RefChangeSystem<EntityStore, WaterbreathingComponent> {

    @Nonnull
    @Override
    public ComponentType<EntityStore, WaterbreathingComponent> componentType() {
        return WaterbreathingComponent.getComponentType();
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return WaterbreathingComponent.getComponentType();
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull WaterbreathingComponent component,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        recheck(ref, store, commandBuffer);
    }

    @Override
    public void onComponentSet(
            @Nonnull Ref<EntityStore> ref,
            @Nullable WaterbreathingComponent oldComponent,
            @Nonnull WaterbreathingComponent newComponent,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        recheck(ref, store, commandBuffer);
    }

    @Override
    public void onComponentRemoved(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull WaterbreathingComponent component,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        recheck(ref, store, commandBuffer);
    }

    private static void recheck(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        BreathingComponent breathing = store.getComponent(ref, BreathingComponent.getComponentType());
        if (breathing == null) return;

        CachedStatsComponent cachedStats = store.getComponent(ref, CachedStatsComponent.getComponentType());
        boolean invulnerable = store.getComponent(ref, Invulnerable.getComponentType()) != null;

        EntityUtils.processEntityBreathing(ref, breathing, cachedStats, invulnerable, true, commandBuffer);
    }
}
