package com.riprod.effectly.systems;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.ability.AbilityConditionSpec;
import com.riprod.effectly.ability.AbilityValue;
import com.riprod.effectly.core.AbilityEntry;
import com.riprod.effectly.core.AbilityGrant;
import com.riprod.effectly.core.AbilityRoster;
import com.riprod.effectly.core.condition.AbilityConditionContext;
import com.riprod.effectly.core.condition.AbilityConditions;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Evaluates whether an ability is currently active for a player, taking into account
 * optional conditions (e.g. in_zone). Use this for abilities that have conditions;
 * systems use this so that zone and other conditions are respected.
 * <p>
 * Debug logging: set the log level for this class to FINE (e.g. in your logging config)
 * to see when conditions are evaluated and whether they pass (e.g. in_zone(3): currentZone=2, passed=false).
 */
public final class AbilityConditionService {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private AbilityConditionService() {}

    /**
     * Returns true if the ability is set for the player and all its conditions pass
     * in the current context (e.g. player position for zone).
     */
    public static boolean isAbilityActive(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world,
            @Nonnull java.util.UUID playerId,
            @Nonnull String abilityId) {
        return getActiveAbilityValue(ref, store, world, playerId, abilityId) != null;
    }

    /**
     * Returns the ability value if the ability is set and all conditions pass; otherwise null.
     * Use this when applying conditional abilities (e.g. stamina_regen in zone 3).
     */
    @Nullable
    public static AbilityValue getActiveAbilityValue(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world,
            @Nonnull java.util.UUID playerId,
            @Nonnull String abilityId) {
        return getActiveAbilityValue(ref, store, world, playerId, abilityId, null);
    }

    /**
     * Like {@link #getActiveAbilityValue(Ref, ComponentAccessor, World, java.util.UUID, String)} but supports
     * target-based conditions (e.g. target_health_below). Pass the <b>target</b> entity ref when applying
     * the ability in a damage context (e.g. the entity being damaged). If targetRef is null, target-based
     * conditions fail.
     */
    @Nullable
    public static AbilityValue getActiveAbilityValue(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world,
            @Nonnull java.util.UUID playerId,
            @Nonnull String abilityId,
            @Nullable Ref<EntityStore> targetRef) {
        AbilityRoster roster = AbilityRoster.of(ref, store);
        AbilityEntry entry = roster != null ? roster.get(abilityId) : null;
        if (entry == null) return null;

        double best = 0.0;
        boolean any = false;
        for (Map.Entry<String, AbilityGrant> granted : entry.getGrants().entrySet()) {
            AbilityGrant grant = granted.getValue();
            if (!conditionsPass(ref, store, world, abilityId, granted.getKey(), grant, targetRef)) continue;
            if (!any || grant.getValue() > best) best = grant.getValue();
            any = true;
        }
        if (!any) {
            LOGGER.at(Level.FINE).log("No active source for ability '%s' -> inactive", abilityId);
            return null;
        }
        LOGGER.at(Level.FINE).log("Ability '%s' active at %s", abilityId, best);
        return AbilityEntry.toValue(abilityId, best);
    }

    private static boolean conditionsPass(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world,
            @Nonnull String abilityId,
            @Nonnull String sourceId,
            @Nonnull AbilityGrant grant,
            @Nullable Ref<EntityStore> targetRef) {
        List<AbilityConditionSpec> conditions = grant.getConditions();
        if (conditions.isEmpty()) return true;

        LOGGER.at(Level.FINE).log("Evaluating %d condition(s) for ability '%s' source '%s'",
                conditions.size(), abilityId, sourceId);
        for (AbilityConditionSpec cond : conditions) {
            boolean passed = evaluate(ref, store, world, abilityId, cond, targetRef);
            LOGGER.at(Level.FINE).log("  Condition %s(%s): passed=%s", cond.type(), cond.param(), passed);
            if (!passed) return false;
        }
        return true;
    }

    private static boolean evaluate(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world,
            @Nonnull String abilityId,
            @Nonnull AbilityConditionSpec cond,
            @Nullable Ref<EntityStore> targetRef) {
        return AbilityConditions.test(new AbilityConditionContext(ref, store, world, targetRef), cond);
    }
}
