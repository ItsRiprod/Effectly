package com.riprod.abilityapi.ability;

import javax.annotation.Nonnull;

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
