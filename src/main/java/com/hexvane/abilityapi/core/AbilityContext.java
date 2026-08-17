package com.hexvane.abilityapi.core;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public final class AbilityContext {

    private final Ref<EntityStore> ref;
    private final ComponentAccessor<EntityStore> components;
    private final World world;
    private final PlayerRef playerRef;

    public AbilityContext(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> components,
            @Nonnull World world,
            @Nonnull PlayerRef playerRef) {
        this.ref = ref;
        this.components = components;
        this.world = world;
        this.playerRef = playerRef;
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

    @Nonnull
    public PlayerRef getPlayerRef() {
        return playerRef;
    }

    @Nonnull
    public UUID getUuid() {
        return playerRef.getUuid();
    }
}
