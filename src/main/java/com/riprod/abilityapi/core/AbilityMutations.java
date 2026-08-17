package com.riprod.abilityapi.core;

import com.riprod.abilityapi.ability.AbilityConditionSpec;
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
        grant(playerId, abilityId, AbilitySources.API, value, conditions, true);
    }

    public static void grant(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull String sourceId,
            double value,
            @Nullable List<AbilityConditionSpec> conditions,
            boolean persistent) {
        mutate(playerId, abilityId,
                roster -> applyGrant(roster, abilityId, sourceId, value, conditions, persistent),
                (context, handler) -> grantIn(context, abilityId, sourceId, value, conditions, persistent));
    }

    public static void setConditions(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull List<AbilityConditionSpec> conditions) {
        setConditions(playerId, abilityId, AbilitySources.API, conditions);
    }

    public static void setConditions(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull String sourceId,
            @Nonnull List<AbilityConditionSpec> conditions) {
        mutate(playerId, abilityId,
                roster -> {
                    AbilityEntry entry = roster.get(abilityId);
                    AbilityGrant grant = entry != null ? entry.getGrant(sourceId) : null;
                    if (grant != null) grant.setConditions(conditions);
                },
                (context, handler) -> {
                    AbilityRoster roster = context.getRoster();
                    AbilityEntry entry = roster != null ? roster.get(abilityId) : null;
                    AbilityGrant grant = entry != null ? entry.getGrant(sourceId) : null;
                    if (grant == null) return;
                    grant.setConditions(conditions);
                    handler.grant(context, abilityId, entry);
                });
    }

    public static void revoke(@Nonnull UUID playerId, @Nonnull String abilityId) {
        revoke(playerId, abilityId, AbilitySources.API);
    }

    public static void revoke(@Nonnull UUID playerId, @Nonnull String abilityId, @Nonnull String sourceId) {
        mutate(playerId, abilityId,
                roster -> applyRevoke(roster, abilityId, sourceId),
                (context, handler) -> revokeIn(context, abilityId, sourceId));
    }

    public static void grantIn(
            @Nonnull AbilityContext context,
            @Nonnull String abilityId,
            @Nonnull String sourceId,
            double value,
            @Nullable List<AbilityConditionSpec> conditions,
            boolean persistent) {
        AbilityHandler handler = AbilityHandlerRegistry.forAbility(abilityId);
        if (handler == null) {
            LOGGER.atWarning().log("No handler registered for ability '%s'; ignoring", abilityId);
            return;
        }
        AbilityRoster roster = context.getOrCreateRoster();
        AbilityEntry entry = applyGrant(roster, abilityId, sourceId, value, conditions, persistent);
        handler.grant(context, abilityId, entry);
    }

    public static void revokeIn(
            @Nonnull AbilityContext context,
            @Nonnull String abilityId,
            @Nonnull String sourceId) {
        AbilityHandler handler = AbilityHandlerRegistry.forAbility(abilityId);
        if (handler == null) return;
        AbilityRoster roster = context.getRoster();
        if (roster == null) return;

        AbilityEntry entry = roster.get(abilityId);
        if (entry == null || entry.removeGrant(sourceId) == null) return;

        if (!entry.isEmpty()) {
            handler.grant(context, abilityId, entry);
            return;
        }
        roster.remove(abilityId);
        handler.revoke(context, abilityId);
        if (roster.isEmpty()) {
            context.clearRoster();
        }
    }

    @Nonnull
    private static AbilityEntry applyGrant(
            @Nonnull AbilityRoster roster,
            @Nonnull String abilityId,
            @Nonnull String sourceId,
            double value,
            @Nullable List<AbilityConditionSpec> conditions,
            boolean persistent) {
        AbilityEntry entry = roster.get(abilityId);
        AbilityGrant previous = entry != null ? entry.getGrant(sourceId) : null;
        List<AbilityConditionSpec> resolved = conditions != null
                ? conditions
                : (previous != null ? previous.getConditions() : List.of());
        AbilityGrant grant = new AbilityGrant(value, resolved, persistent);
        if (entry == null) {
            entry = new AbilityEntry(sourceId, grant);
            roster.put(abilityId, entry);
        } else {
            entry.putGrant(sourceId, grant);
        }
        return entry;
    }

    private static void applyRevoke(
            @Nonnull AbilityRoster roster,
            @Nonnull String abilityId,
            @Nonnull String sourceId) {
        AbilityEntry entry = roster.get(abilityId);
        if (entry == null) return;
        entry.removeGrant(sourceId);
        if (entry.isEmpty()) roster.remove(abilityId);
    }

    public static void applyAll(@Nonnull AbilityContext context) {
        AbilityRoster roster = context.getRoster();
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
}
