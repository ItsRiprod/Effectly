package com.riprod.abilityapi.core.stat;

import com.riprod.abilityapi.core.AbilityContext;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public final class AbilityStatApplier {

    public static final String MODIFIER_KEY = "Ability_ADDITIVE";

    private AbilityStatApplier() {}

    public static void apply(@Nonnull AbilityContext context) {
        StatAccumulator accumulator = StatContributions.accumulate(context);
        StatPacingComponent pacing = context.getComponents()
                .getComponent(context.getRef(), StatPacingComponent.getComponentType());

        applyStatModifiers(context, accumulator, pacing);
        applyBaseSpeed(context, accumulator);
    }

    private static void applyStatModifiers(
            @Nonnull AbilityContext context,
            @Nonnull StatAccumulator accumulator,
            StatPacingComponent pacing) {
        EntityStatMap statMap = context.getComponents()
                .getComponent(context.getRef(), EntityStatMap.getComponentType());
        if (statMap == null) return;

        Map<Integer, Float> additions = accumulator.getMaxStatAdditions();
        Set<Integer> previouslyApplied = pacing != null ? pacing.getAppliedStats() : Set.of();

        for (int statIndex : previouslyApplied) {
            if (additions.containsKey(statIndex)) continue;
            if (statIndex < 0 || statIndex >= statMap.size()) continue;
            statMap.removeModifier(EntityStatMap.Predictable.SELF, statIndex, MODIFIER_KEY);
        }

        Set<Integer> applied = new LinkedHashSet<>();
        for (Map.Entry<Integer, Float> addition : additions.entrySet()) {
            int statIndex = addition.getKey();
            if (statIndex < 0 || statIndex >= statMap.size()) continue;
            statMap.putModifier(EntityStatMap.Predictable.SELF, statIndex, MODIFIER_KEY,
                    new StaticModifier(Modifier.ModifierTarget.MAX,
                            StaticModifier.CalculationType.ADDITIVE, addition.getValue()));
            applied.add(statIndex);
        }

        if (pacing != null) pacing.setAppliedStats(applied);
        if (!applied.isEmpty() || !previouslyApplied.isEmpty()) {
            statMap.getStatModifiersManager().scheduleRecalculate();
        }
    }

    private static void applyBaseSpeed(@Nonnull AbilityContext context, @Nonnull StatAccumulator accumulator) {
        MovementManager movementManager = context.getComponents()
                .getComponent(context.getRef(), EntityModule.get().getMovementManagerComponentType());
        if (movementManager == null) return;

        float target = movementManager.getDefaultSettings().baseSpeed * accumulator.getBaseSpeedMultiplier();
        if (movementManager.getSettings().baseSpeed == target) return;
        movementManager.getSettings().baseSpeed = target;
        movementManager.update(context.getPlayerRef().getPacketHandler());
    }
}
