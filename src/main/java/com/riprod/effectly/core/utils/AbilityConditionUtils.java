package com.riprod.effectly.core.utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityComponent;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.component.AbilityGrant;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.effects.registry.EffectAsset;
import com.riprod.effectly.core.effects.registry.EffectHandlerConfig;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;
import com.riprod.effectly.core.conditions.ConditionContext;
import com.riprod.effectly.core.conditions.registry.ConditionRegistry;
import com.riprod.effectly.core.effects.components.AbilityValue;

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
public final class AbilityConditionUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    /** Sentinel for "not active"; distinguishable from a real value of zero. */
    public static final double INACTIVE = Double.NaN;

    private AbilityConditionUtils() {}

    public static boolean isActive(double value) {
        return !Double.isNaN(value);
    }

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
        return isActive(activeValue(ref, store, world, playerId, abilityId));
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
        double value = activeValue(ref, store, world, playerId, abilityId, targetRef);
        return isActive(value) ? AbilityEntry.toValue(abilityId, value) : null;
    }

    /**
     * Allocation-free form of {@link #getActiveAbilityValue}. Returns {@link #INACTIVE} when the
     * ability is absent or gated. Prefer this everywhere except the command and public API surface,
     * where the boolean-vs-number distinction is user-facing.
     */
    public static double activeValue(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world,
            @Nonnull java.util.UUID playerId,
            @Nonnull String abilityId) {
        return activeValue(ref, store, world, playerId, abilityId, null);
    }

    public static double activeValue(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world,
            @Nonnull java.util.UUID playerId,
            @Nonnull String abilityId,
            @Nullable Ref<EntityStore> targetRef) {
        AbilityComponent roster = AbilityComponent.of(ref, store);
        AbilityEntry entry = roster != null ? roster.get(abilityId) : null;
        if (entry == null) return INACTIVE;

        List<AbilityConditionSpec> intrinsic = EffectAsset.conditionsFor(abilityId);
        if (!intrinsic.isEmpty()
                && !allPass(ref, store, world, abilityId, intrinsic, targetRef)) {
            LOGGER.at(Level.FINE).log("Ability '%s' gated by its own conditions -> inactive", abilityId);
            return INACTIVE;
        }

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
            return INACTIVE;
        }
        LOGGER.at(Level.FINE).log("Ability '%s' active at %s", abilityId, best);
        return best;
    }

    /**
     * Resolves which ability implemented by {@code handlerId} is currently active on this entity,
     * and at what value. A handler may back several assets, so a system must ask by handler rather
     * than assuming its handler id is also an ability id. Highest active value wins, matching how
     * sources are resolved within a single ability.
     */
    @Nullable
    public static ActiveAbility bestActiveForHandler(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world,
            @Nonnull java.util.UUID playerId,
            @Nonnull String handlerId) {
        return bestActiveForHandler(ref, store, world, playerId, handlerId, null);
    }

    @Nullable
    public static ActiveAbility bestActiveForHandler(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world,
            @Nonnull java.util.UUID playerId,
            @Nonnull String handlerId,
            @Nullable Ref<EntityStore> targetRef) {
        List<ResolvedAbilityComponent.Resolved> candidates =
                ResolvedAbilityComponent.forHandler(ref, store, handlerId);
        if (candidates.isEmpty()) return null;

        ActiveAbility best = null;
        for (ResolvedAbilityComponent.Resolved candidate : candidates) {
            double amount = activeValue(ref, store, world, playerId, candidate.abilityId(), targetRef);
            if (!isActive(amount)) continue;
            if (best == null || amount > best.value()) {
                best = new ActiveAbility(candidate.abilityId(), candidate.assetIndex(), amount);
            }
        }
        return best;
    }

    /**
     * @param assetIndex resolved index into the effect asset map, for array-speed config lookups
     */
    public record ActiveAbility(@Nonnull String abilityId, int assetIndex, double value) {

        @Nullable
        public <T extends EffectHandlerConfig> T config(@Nonnull Class<T> type) {
            EffectAsset asset = EffectAsset.byIndex(assetIndex);
            EffectHandlerConfig config = asset != null ? asset.getHandlerConfig() : null;
            return type.isInstance(config) ? type.cast(config) : null;
        }

        @Nonnull
        public <T extends EffectHandlerConfig> T configOrDefault(@Nonnull Class<T> type, @Nonnull T fallback) {
            T config = config(type);
            return config != null ? config : fallback;
        }
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

        return allPass(ref, store, world, abilityId, conditions, targetRef);
    }

    private static boolean allPass(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world,
            @Nonnull String abilityId,
            @Nonnull List<AbilityConditionSpec> conditions,
            @Nullable Ref<EntityStore> targetRef) {
        ConditionContext context = new ConditionContext(ref, store, world, targetRef);
        for (AbilityConditionSpec cond : conditions) {
            if (!ConditionRegistry.test(context, cond)) {
                LOGGER.at(Level.FINE).log("Condition '%s' failed for ability '%s'", cond.type(), abilityId);
                return false;
            }
        }
        return true;
    }

}
