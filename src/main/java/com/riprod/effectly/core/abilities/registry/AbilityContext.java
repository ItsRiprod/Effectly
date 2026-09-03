package com.riprod.effectly.core.abilities.registry;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityComponent;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.actions.component.ActionHolderComponent;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityContext {

    private final Ref<EntityStore> ref;
    private final ComponentAccessor<EntityStore> components;
    private final World world;
    private final PlayerRef playerRef;

    private AbilityComponent roster;
    private ResolvedAbilityComponent resolved;

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
        // only a found roster is cached; latching a null would hide one created by another path
        if (roster == null) {
            roster = AbilityComponent.of(ref, components);
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
        components.tryRemoveComponent(ref, ResolvedAbilityComponent.getComponentType());
        components.tryRemoveComponent(ref, ActionHolderComponent.getComponentType());
        roster = null;
        resolved = null;
    }

    /**
     * Rebuilds the handler-grouped view of the roster. Call after any roster mutation and before
     * dispatching to handlers, so a handler always sees the abilities it currently backs.
     * <p>
     * The view is held here for the life of the context because a CommandBuffer accessor reads the
     * live store rather than its own pending writes; without this a second rebuild in one apply pass
     * would start from a stale instance.
     */
    public void refreshResolved() {
        AbilityComponent current = getRoster();
        if (current == null || current.isEmpty()) {
            components.tryRemoveComponent(ref, ResolvedAbilityComponent.getComponentType());
            components.tryRemoveComponent(ref, ActionHolderComponent.getComponentType());
            resolved = null;
            return;
        }
        ResolvedAbilityComponent view = resolved != null
                ? resolved
                : components.getComponent(ref, ResolvedAbilityComponent.getComponentType());
        if (view == null) view = new ResolvedAbilityComponent();
        view.resolveFrom(current);
        resolved = view;
        components.putComponent(ref, ResolvedAbilityComponent.getComponentType(), view);

        if (view.hasActions()) {
            if (components.getComponent(ref, ActionHolderComponent.getComponentType()) == null) {
                components.putComponent(ref, ActionHolderComponent.getComponentType(), new ActionHolderComponent());
            }
        } else {
            components.tryRemoveComponent(ref, ActionHolderComponent.getComponentType());
        }
    }
}
