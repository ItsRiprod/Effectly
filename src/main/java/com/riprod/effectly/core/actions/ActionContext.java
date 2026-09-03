package com.riprod.effectly.core.actions;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.actions.capability.Capability;
import com.riprod.effectly.core.actions.capability.CapabilityRegistry;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ActionContext {

    private final Capability[] slots;
    private final CommandBuffer<EntityStore> entities;
    private final World world;

    private ActionContext(
            @Nonnull Capability[] slots,
            @Nonnull CommandBuffer<EntityStore> entities,
            @Nonnull World world) {
        this.slots = slots;
        this.entities = entities;
        this.world = world;
    }

    @Nonnull
    public static Builder builder(
            @Nonnull CommandBuffer<EntityStore> entities,
            @Nonnull World world) {
        return new Builder(entities, world);
    }

    @Nonnull
    public <T extends Capability> T get(@Nonnull CapabilityType<T> type) {
        Capability capability = find(type);
        if (capability == null) {
            throw new IllegalStateException(
                    "Capability '" + type.getId() + "' was required but not supplied by the trigger");
        }
        return type.getType().cast(capability);
    }

    @Nullable
    public Capability find(@Nonnull CapabilityType<?> type) {
        int index = type.getIndex();
        return index >= 0 && index < slots.length ? slots[index] : null;
    }

    public boolean has(@Nonnull CapabilityType<?> type) {
        return find(type) != null;
    }

    @Nonnull
    public CommandBuffer<EntityStore> getEntityStore() {
        return entities;
    }

    @Nonnull
    public ChunkStore getChunkStore() {
        return world.getChunkStore();
    }

    @Nonnull
    public World getWorld() {
        return world;
    }

    public static final class Builder {

        private final Capability[] slots;
        private final CommandBuffer<EntityStore> entities;
        private final World world;

        private Builder(@Nonnull CommandBuffer<EntityStore> entities, @Nonnull World world) {
            this.slots = new Capability[CapabilityRegistry.count()];
            this.entities = entities;
            this.world = world;
        }

        @Nonnull
        public <T extends Capability> Builder with(@Nonnull CapabilityType<T> type, @Nonnull T capability) {
            int index = type.getIndex();
            if (index < 0 || index >= slots.length) {
                throw new IllegalStateException(
                        "Capability '" + type.getId() + "' is not registered; register it before the asset store");
            }
            slots[index] = capability;
            return this;
        }

        @Nonnull
        public ActionContext build() {
            return new ActionContext(slots, entities, world);
        }
    }
}
