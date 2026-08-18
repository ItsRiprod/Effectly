package com.riprod.effectly.core.condition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.effectly.ability.AbilityConditionSpec;

public interface AbilityCondition {

    @Nonnull
    String getId();

    boolean test(@Nonnull AbilityConditionContext context, @Nonnull AbilityConditionSpec spec);

    @Nonnull
    default String keyword() {
        return getId();
    }

    @Nullable
    default Parsed parse(@Nonnull String[] remaining) {
        return null;
    }

    @Nonnull
    default String describe(@Nonnull AbilityConditionSpec spec) {
        return getId();
    }

    @Nonnull
    String usage();

    @Nonnull
    String description();

    record Parsed(@Nonnull AbilityConditionSpec spec, int consumed) {}
}
