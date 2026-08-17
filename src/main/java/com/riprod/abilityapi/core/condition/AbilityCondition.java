package com.riprod.abilityapi.core.condition;

import com.riprod.abilityapi.ability.AbilityConditionSpec;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
