package com.riprod.effectly.core.effects.registry;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.registry.AbilityContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface EffectHandler {

    @Nonnull
    String getId();

    @Nullable
    default ConfigBinding<? extends EffectHandlerConfig> getConfigBinding() {
        return null;
    }

    @Nullable
    default <T extends EffectHandlerConfig> T getConfig(@Nonnull Class<T> type, @Nullable EffectAsset asset) {
        if (asset == null) return null;
        EffectHandlerConfig config = asset.getHandlerConfig();
        return type.isInstance(config) ? type.cast(config) : null;
    }

    @Nullable
    default <T extends EffectHandlerConfig> T getConfig(@Nonnull Class<T> type, @Nonnull String abilityId) {
        return getConfig(type, EffectAsset.getAssetMap().getAsset(abilityId));
    }

    record ConfigBinding<T extends EffectHandlerConfig>(@Nonnull Class<T> type, @Nonnull BuilderCodec<T> codec) {
        @Nonnull
        public static <T extends EffectHandlerConfig> ConfigBinding<T> of(
                @Nonnull Class<T> type, @Nonnull BuilderCodec<T> codec) {
            return new ConfigBinding<>(type, codec);
        }
    }

    default void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
    }

    default void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
    }

    default void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
    }

    default void reconcile(@Nonnull AbilityContext context) {
    }
}
