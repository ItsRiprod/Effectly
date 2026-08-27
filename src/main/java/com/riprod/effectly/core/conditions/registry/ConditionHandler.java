package com.riprod.effectly.core.conditions.registry;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;
import com.riprod.effectly.core.conditions.ConditionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ConditionHandler {

    @Nonnull
    String getId();

    @Nullable
    default ConfigBinding<? extends ConditionConfig> getConfigBinding() {
        return null;
    }

    boolean test(
            @Nonnull ConditionContext context,
            @Nonnull ConditionAsset asset,
            @Nonnull AbilityConditionSpec spec);

    @Nullable
    default Parsed parse(@Nonnull ConditionAsset asset, @Nonnull String[] remaining) {
        return null;
    }

    @Nonnull
    default String describe(@Nonnull ConditionAsset asset, @Nonnull AbilityConditionSpec spec) {
        return asset.getKeyword();
    }

    @Nonnull
    default String argumentUsage() {
        return "";
    }

    record ConfigBinding<T extends ConditionConfig>(
            @Nonnull Class<T> type, @Nonnull BuilderCodec<T> codec) {
        @Nonnull
        public static <T extends ConditionConfig> ConfigBinding<T> of(
                @Nonnull Class<T> type, @Nonnull BuilderCodec<T> codec) {
            return new ConfigBinding<>(type, codec);
        }
    }

    record Parsed(@Nonnull AbilityConditionSpec spec, int consumed) {}
}
