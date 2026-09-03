package com.riprod.effectly.core.actions;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ActionContext {

    private final Ref<EntityStore> holderRef;
    private final Store<EntityStore> store;
    private final CommandBuffer<EntityStore> commandBuffer;
    private final World world;
    private final double value;
    private final double magnitude;
    private final org.joml.Vector3d position;
    private final Ref<EntityStore> otherRef;
    private final Damage damage;

    public ActionContext(
            @Nonnull Ref<EntityStore> holderRef,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull World world,
            double value,
            double magnitude,
            @Nonnull org.joml.Vector3d position,
            @Nullable Ref<EntityStore> otherRef,
            @Nullable Damage damage) {
        this.holderRef = holderRef;
        this.store = store;
        this.commandBuffer = commandBuffer;
        this.world = world;
        this.value = value;
        this.magnitude = magnitude;
        this.position = position;
        this.otherRef = otherRef;
        this.damage = damage;
    }

    @Nonnull
    public Ref<EntityStore> getHolderRef() {
        return holderRef;
    }

    @Nonnull
    public Store<EntityStore> getStore() {
        return store;
    }

    @Nonnull
    public CommandBuffer<EntityStore> getCommandBuffer() {
        return commandBuffer;
    }

    @Nonnull
    public World getWorld() {
        return world;
    }

    public double getValue() {
        return value;
    }

    public double getMagnitude() {
        return magnitude;
    }

    @Nonnull
    public org.joml.Vector3d getPosition() {
        return position;
    }

    @Nullable
    public Ref<EntityStore> getOtherRef() {
        return otherRef;
    }

    @Nullable
    public Damage getDamage() {
        return damage;
    }
}
