package com.riprod.effectly.core.conditions.components;

import javax.annotation.Nonnull;

import com.riprod.effectly.core.effects.utils.AbilityType;

/**
 * Immutable definition of an ability.
 */
public record AbilityDefinition(
        @Nonnull String id,
        @Nonnull AbilityType type,
        @Nonnull Object defaultValue,
        double min,
        double max,
        @Nonnull String description
) {
}
