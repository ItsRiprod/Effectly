package com.riprod.effectly.builtin.effects.darkvision;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class DarkVisionCleanupSystem extends RefChangeSystem<EntityStore, DarkVisionComponent> {

    @Nonnull
    @Override
    public ComponentType<EntityStore, DarkVisionComponent> componentType() {
        return DarkVisionComponent.getComponentType();
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return DarkVisionComponent.getComponentType();
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull DarkVisionComponent component,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    }

    @Override
    public void onComponentSet(
            @Nonnull Ref<EntityStore> ref,
            @Nullable DarkVisionComponent oldComponent,
            @Nonnull DarkVisionComponent newComponent,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    }

    @Override
    public void onComponentRemoved(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull DarkVisionComponent component,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        DarkVisionEffects.remove(ref, commandBuffer,
                component.configOrDefault(DarkVisionConfig.class, DarkVisionConfig.DEFAULTS));
    }
}
