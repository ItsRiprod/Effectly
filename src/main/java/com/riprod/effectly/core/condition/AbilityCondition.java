package com.riprod.effectly.core.condition;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.effectly.ability.AbilityConditionSpec;
import com.riprod.effectly.core.condition.asset.AbilityConditionAsset;
import com.riprod.effectly.core.condition.asset.AbilityConditionConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface AbilityCondition {

    @Nonnull
    String getId();

    @Nullable
    default ConfigBinding<? extends AbilityConditionConfig> getConfigBinding() {
        return null;
    }

    boolean test(
            @Nonnull AbilityConditionContext context,
            @Nonnull AbilityConditionAsset asset,
            @Nonnull AbilityConditionSpec spec);

    @Nullable
    default Parsed parse(@Nonnull AbilityConditionAsset asset, @Nonnull String[] remaining) {
        return null;
    }

    @Nonnull
    default String describe(@Nonnull AbilityConditionAsset asset, @Nonnull AbilityConditionSpec spec) {
        return asset.getKeyword();
    }

    @Nonnull
    default String argumentUsage() {
        return "";
    }

    record ConfigBinding<T extends AbilityConditionConfig>(
            @Nonnull Class<T> type, @Nonnull BuilderCodec<T> codec) {
        @Nonnull
        public static <T extends AbilityConditionConfig> ConfigBinding<T> of(
                @Nonnull Class<T> type, @Nonnull BuilderCodec<T> codec) {
            return new ConfigBinding<>(type, codec);
        }
    }

    record Parsed(@Nonnull AbilityConditionSpec spec, int consumed) {}
}
