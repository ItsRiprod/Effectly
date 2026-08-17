package com.riprod.abilityapi.core.condition;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityConditionContext {

    private final Ref<EntityStore> ref;
    private final ComponentAccessor<EntityStore> components;
    private final World world;
    private final Ref<EntityStore> targetRef;

    public AbilityConditionContext(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> components,
            @Nonnull World world,
            @Nullable Ref<EntityStore> targetRef) {
        this.ref = ref;
        this.components = components;
        this.world = world;
        this.targetRef = targetRef;
    }

    @Nonnull
    public Ref<EntityStore> getRef() {
        return ref;
    }

    @Nonnull
    public ComponentAccessor<EntityStore> getComponents() {
        return components;
    }

    @Nonnull
    public World getWorld() {
        return world;
    }

    @Nullable
    public Ref<EntityStore> getTargetRef() {
        return targetRef;
    }
}
