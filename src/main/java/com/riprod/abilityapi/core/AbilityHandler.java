package com.riprod.abilityapi.core;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.abilityapi.core.asset.AbilityAsset;
import com.riprod.abilityapi.core.asset.AbilityHandlerConfig;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface AbilityHandler {

    @Nonnull
    String getId();

    @Nullable
    default ConfigBinding<? extends AbilityHandlerConfig> getConfigBinding() {
        return null;
    }

    @Nullable
    default <T extends AbilityHandlerConfig> T getConfig(@Nonnull Class<T> type, @Nullable AbilityAsset asset) {
        if (asset == null) return null;
        AbilityHandlerConfig config = asset.getHandlerConfig();
        return type.isInstance(config) ? type.cast(config) : null;
    }

    @Nullable
    default <T extends AbilityHandlerConfig> T getConfig(@Nonnull Class<T> type, @Nonnull String abilityId) {
        return getConfig(type, AbilityAsset.getAssetMap().getAsset(abilityId));
    }

    record ConfigBinding<T extends AbilityHandlerConfig>(@Nonnull Class<T> type, @Nonnull BuilderCodec<T> codec) {
        @Nonnull
        public static <T extends AbilityHandlerConfig> ConfigBinding<T> of(
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
