package com.riprod.effectly.core.utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityComponent;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.component.AbilityGrant;
import com.riprod.effectly.core.abilities.registry.AbilityContext;
import com.riprod.effectly.core.abilities.registry.AbilityHandlerRegistry;
import com.riprod.effectly.core.abilities.utils.AbilitySourcesUtils;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;
import com.riprod.effectly.core.effects.registry.EffectAsset;
import com.riprod.effectly.core.effects.registry.EffectHandler;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityMutationUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private AbilityMutationUtils() {}

    public static void grant(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            double value,
            @Nullable List<AbilityConditionSpec> conditions) {
        grant(playerId, abilityId, AbilitySourcesUtils.API, value, conditions, true);
    }

    public static void grant(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull String sourceId,
            double value,
            @Nullable List<AbilityConditionSpec> conditions,
            boolean persistent) {
        if (!enabledOrWarn(abilityId)) return;
        mutate(playerId, abilityId,
                roster -> applyGrant(roster, abilityId, sourceId, value, conditions, persistent),
                (context, handler) -> grantIn(context, abilityId, sourceId, value, conditions, persistent));
    }

    public static void setConditions(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull List<AbilityConditionSpec> conditions) {
        setConditions(playerId, abilityId, AbilitySourcesUtils.API, conditions);
    }

    public static void setConditions(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull String sourceId,
            @Nonnull List<AbilityConditionSpec> conditions) {
        if (!enabledOrWarn(abilityId)) return;
        mutate(playerId, abilityId,
                roster -> {
                    AbilityEntry entry = roster.get(abilityId);
                    AbilityGrant grant = entry != null ? entry.getGrant(sourceId) : null;
                    if (grant != null) grant.setConditions(conditions);
                },
                (context, handler) -> {
                    AbilityComponent roster = context.getRoster();
                    AbilityEntry entry = roster != null ? roster.get(abilityId) : null;
                    AbilityGrant grant = entry != null ? entry.getGrant(sourceId) : null;
                    if (grant == null) return;
                    grant.setConditions(conditions);
                    handler.grant(context, abilityId, entry);
                });
    }

    public static void revoke(@Nonnull UUID playerId, @Nonnull String abilityId) {
        revoke(playerId, abilityId, AbilitySourcesUtils.API);
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
        if (!enabledOrWarn(abilityId)) return;
        EffectHandler handler = AbilityHandlerRegistry.forAbility(abilityId);
        if (handler == null) {
            LOGGER.atWarning().log("No handler registered for ability '%s'; ignoring", abilityId);
            return;
        }
        AbilityComponent roster = context.getOrCreateRoster();
        AbilityEntry entry = applyGrant(roster, abilityId, sourceId, value, conditions, persistent);
        handler.grant(context, abilityId, entry);
    }

    public static void revokeIn(
            @Nonnull AbilityContext context,
            @Nonnull String abilityId,
            @Nonnull String sourceId) {
        EffectHandler handler = AbilityHandlerRegistry.forAbility(abilityId);
        if (handler == null) return;
        AbilityComponent roster = context.getRoster();
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
            @Nonnull AbilityComponent roster,
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
        double clamped = EffectAsset.clampToRange(abilityId, value);
        if (clamped != value) {
            LOGGER.atWarning().atMostEvery(1, TimeUnit.MINUTES)
                    .log("Source '%s' granted '%s' as %s, outside the range declared by the asset; clamped to %s",
                            sourceId, abilityId, value, clamped);
        }
        AbilityGrant grant = new AbilityGrant(clamped, resolved, persistent);
        if (entry == null) {
            entry = new AbilityEntry(sourceId, grant);
            roster.put(abilityId, entry);
        } else {
            entry.putGrant(sourceId, grant);
        }
        return entry;
    }

    private static boolean enabledOrWarn(@Nonnull String abilityId) {
        if (EffectAsset.isEnabled(abilityId)) return true;
        LOGGER.atWarning().atMostEvery(1, TimeUnit.MINUTES)
                .log("Ability '%s' is disabled or unknown; the grant was ignored", abilityId);
        return false;
    }

    private static void applyRevoke(
            @Nonnull AbilityComponent roster,
            @Nonnull String abilityId,
            @Nonnull String sourceId) {
        AbilityEntry entry = roster.get(abilityId);
        if (entry == null) return;
        entry.removeGrant(sourceId);
        if (entry.isEmpty()) roster.remove(abilityId);
    }

    public static void applyAll(@Nonnull AbilityContext context) {
        AbilityComponent roster = context.getRoster();
        if (roster != null) {
            for (Map.Entry<String, AbilityEntry> granted : roster.getAbilities().entrySet()) {
                if (!EffectAsset.isEnabled(granted.getKey())) {
                    LOGGER.at(Level.FINE).atMostEvery(5, TimeUnit.MINUTES)
                            .log("Ability '%s' is disabled; leaving it in the roster", granted.getKey());
                    continue;
                }
                EffectHandler handler = AbilityHandlerRegistry.forAbility(granted.getKey());
                if (handler == null) {
                    LOGGER.atFine().log("No handler for ability '%s'; leaving it in the roster", granted.getKey());
                    continue;
                }
                handler.grant(context, granted.getKey(), granted.getValue());
            }
        }
        for (EffectHandler handler : AbilityHandlerRegistry.all()) {
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
            @Nonnull Consumer<AbilityComponent> rosterChange,
            @Nonnull BiConsumer<AbilityContext, EffectHandler> whenOnline) {
        EffectHandler handler = AbilityHandlerRegistry.forAbility(abilityId);
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
            @Nonnull Consumer<AbilityComponent> rosterChange) {
        Universe universe = Universe.get();
        if (universe == null || universe.getPlayerStorage() == null) {
            LOGGER.atWarning().log("No player storage; '%s' for offline %s was not applied", abilityId, playerId);
            return;
        }
        universe.getPlayerStorage().update(playerId, holder -> {
            AbilityComponent roster = holder.getComponent(AbilityComponent.getComponentType());
            if (roster == null) {
                roster = new AbilityComponent();
                holder.putComponent(AbilityComponent.getComponentType(), roster);
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
