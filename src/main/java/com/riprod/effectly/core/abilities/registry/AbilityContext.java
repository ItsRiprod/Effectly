package com.riprod.effectly.core.abilities.registry;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityComponent;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityContext {

    private final Ref<EntityStore> ref;
    private final ComponentAccessor<EntityStore> components;
    private final World world;
    private final PlayerRef playerRef;

    private AbilityComponent roster;
    private boolean rosterResolved;

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

    @Nullable
    public AbilityComponent getRoster() {
        if (!rosterResolved) {
            roster = AbilityComponent.of(ref, components);
            rosterResolved = true;
        }
        return roster;
    }

    @Nonnull
    public AbilityComponent getOrCreateRoster() {
        AbilityComponent existing = getRoster();
        if (existing != null) return existing;
        roster = new AbilityComponent();
        components.putComponent(ref, AbilityComponent.getComponentType(), roster);
        return roster;
    }

    public void clearRoster() {
        components.tryRemoveComponent(ref, AbilityComponent.getComponentType());
        roster = null;
        rosterResolved = true;
    }
}
