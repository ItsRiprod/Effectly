package com.hexvane.abilityapi.core;

import com.hexvane.abilityapi.ability.AbilityConditionSpec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityMutations {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private AbilityMutations() {}

    public static void grant(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            double value,
            @Nullable List<AbilityConditionSpec> conditions) {
        mutate(playerId, abilityId,
                roster -> {
                    AbilityEntry previous = roster.get(abilityId);
                    List<AbilityConditionSpec> resolved = conditions != null
                            ? conditions
                            : (previous != null ? previous.getConditions() : List.of());
                    roster.put(abilityId, new AbilityEntry(value, resolved));
                },
                (context, handler) -> handler.grant(context, abilityId, roster(context, true).get(abilityId)));
    }

    public static void setConditions(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull List<AbilityConditionSpec> conditions) {
        mutate(playerId, abilityId,
                roster -> {
                    AbilityEntry entry = roster.get(abilityId);
                    if (entry != null) entry.setConditions(conditions);
                },
                (context, handler) -> {
                    AbilityEntry entry = roster(context, true).get(abilityId);
                    if (entry != null) handler.grant(context, abilityId, entry);
                });
    }

    public static void revoke(@Nonnull UUID playerId, @Nonnull String abilityId) {
        mutate(playerId, abilityId,
                roster -> roster.remove(abilityId),
                (context, handler) -> {
                    handler.revoke(context, abilityId);
                    AbilityRoster roster = AbilityRoster.of(context.getRef(), context.getComponents());
                    if (roster != null && roster.isEmpty()) {
                        context.getComponents().removeComponent(context.getRef(), AbilityRoster.getComponentType());
                    }
                });
    }

    public static void applyAll(@Nonnull AbilityContext context) {
        AbilityRoster roster = AbilityRoster.of(context.getRef(), context.getComponents());
        if (roster != null) {
            for (Map.Entry<String, AbilityEntry> granted : roster.getAbilities().entrySet()) {
                AbilityHandler handler = AbilityHandlerRegistry.forAbility(granted.getKey());
                if (handler == null) {
                    LOGGER.atFine().log("No handler for ability '%s'; leaving it in the roster", granted.getKey());
                    continue;
                }
                handler.grant(context, granted.getKey(), granted.getValue());
            }
        }
        for (AbilityHandler handler : AbilityHandlerRegistry.all()) {
            if (AbilityHandlerRegistry.holdsAnyFor(context, handler)) continue;
            handler.reconcile(context);
        }
    }

    public static void applyAll(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> components,
            @Nonnull World world) {
        if (!ref.isValid()) return;
        PlayerRef playerRef = components.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;
        applyAll(new AbilityContext(ref, components, world, playerRef));
    }

    private static void mutate(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull Consumer<AbilityRoster> rosterChange,
            @Nonnull BiConsumer<AbilityContext, AbilityHandler> whenOnline) {
        AbilityHandler handler = AbilityHandlerRegistry.forAbility(abilityId);
        if (handler == null) {
            LOGGER.atWarning().log("No handler registered for ability '%s'; ignoring", abilityId);
            return;
        }
        Universe universe = Universe.get();
        PlayerRef playerRef = universe != null ? universe.getPlayer(playerId) : null;
        if (playerRef == null || !playerRef.isValid()) {
            mutateOffline(playerId, abilityId, rosterChange);
            return;
        }
        World world = universe.getWorld(playerRef.getWorldUuid());
        if (world == null) {
            LOGGER.atWarning().log("No world for player %s; '%s' was not applied", playerId, abilityId);
            return;
        }
        try {
            world.execute(() -> {
                Ref<EntityStore> ref = playerRef.getReference();
                if (ref == null || !ref.isValid()) {
                    mutateOffline(playerId, abilityId, rosterChange);
                    return;
                }
                Store<EntityStore> store = world.getEntityStore().getStore();
                AbilityContext context = new AbilityContext(ref, store, world, playerRef);
                rosterChange.accept(roster(context, true));
                whenOnline.accept(context, handler);
            });
        } catch (RuntimeException e) {
            LOGGER.atWarning().log("World %s rejected the ability task for %s; '%s' was not applied",
                    world.getName(), playerId, abilityId);
        }
    }

    private static void mutateOffline(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull Consumer<AbilityRoster> rosterChange) {
        Universe universe = Universe.get();
        if (universe == null || universe.getPlayerStorage() == null) {
            LOGGER.atWarning().log("No player storage; '%s' for offline %s was not applied", abilityId, playerId);
            return;
        }
        universe.getPlayerStorage().update(playerId, holder -> {
            AbilityRoster roster = holder.getComponent(AbilityRoster.getComponentType());
            if (roster == null) {
                roster = new AbilityRoster();
                holder.putComponent(AbilityRoster.getComponentType(), roster);
            }
            rosterChange.accept(roster);
        }).whenComplete((ignored, error) -> {
            if (error != null) {
                LOGGER.atWarning().withCause(error)
                        .log("Failed to write '%s' to the saved data of offline player %s", abilityId, playerId);
                return;
            }
            LOGGER.atInfo().log("Applied '%s' to the saved data of offline player %s", abilityId, playerId);
        });
    }

    @Nullable
    private static AbilityRoster roster(@Nonnull AbilityContext context, boolean create) {
        AbilityRoster roster = AbilityRoster.of(context.getRef(), context.getComponents());
        if (roster != null || !create) return roster;
        roster = new AbilityRoster();
        context.getComponents().putComponent(context.getRef(), AbilityRoster.getComponentType(), roster);
        return roster;
    }
}
