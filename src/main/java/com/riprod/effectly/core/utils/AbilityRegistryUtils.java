package com.riprod.effectly.core.utils;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.riprod.effectly.core.conditions.components.AbilityDefinition;
import com.riprod.effectly.core.effects.registry.EffectAsset;
import com.riprod.effectly.core.effects.utils.AbilityType;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityRegistryUtils {

    private AbilityRegistryUtils() {}

    @Nullable
    public static AbilityDefinition get(@Nonnull String id) {
        EffectAsset asset = asset(id);
        return asset != null ? toDefinition(asset) : null;
    }

    public static boolean isValid(@Nonnull String id) {
        return get(id) != null;
    }

    @Nonnull
    public static Set<String> getAllIds() {
        DefaultAssetMap<String, EffectAsset> map = assetMap();
        return map != null ? new LinkedHashSet<>(map.getAssetMap().keySet()) : Set.of();
    }

    @Nonnull
    private static AbilityDefinition toDefinition(@Nonnull EffectAsset asset) {
        Object defaultValue = asset.getType() == AbilityType.BINARY
                ? Boolean.TRUE
                : asset.getDefaultValue();
        String description = asset.getDescription() != null ? asset.getDescription() : "";
        return new AbilityDefinition(asset.getId(), asset.getType(), defaultValue,
                asset.getMin(), asset.getMax(), description);
    }

    @Nullable
    private static EffectAsset asset(@Nonnull String id) {
        DefaultAssetMap<String, EffectAsset> map = assetMap();
        return map != null ? map.getAsset(id) : null;
    }

    @Nullable
    private static DefaultAssetMap<String, EffectAsset> assetMap() {
        try {
            return EffectAsset.getAssetMap();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
