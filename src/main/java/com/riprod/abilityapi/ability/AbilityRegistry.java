package com.riprod.abilityapi.ability;

import com.riprod.abilityapi.core.asset.AbilityAsset;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityRegistry {

    private AbilityRegistry() {}

    @Nullable
    public static AbilityDefinition get(@Nonnull String id) {
        AbilityAsset asset = asset(id);
        return asset != null ? toDefinition(asset) : null;
    }

    public static boolean isValid(@Nonnull String id) {
        return get(id) != null;
    }

    @Nonnull
    public static Set<String> getAllIds() {
        DefaultAssetMap<String, AbilityAsset> map = assetMap();
        return map != null ? new LinkedHashSet<>(map.getAssetMap().keySet()) : Set.of();
    }

    @Nonnull
    private static AbilityDefinition toDefinition(@Nonnull AbilityAsset asset) {
        Object defaultValue = asset.getType() == AbilityType.BINARY
                ? Boolean.TRUE
                : asset.getDefaultValue();
        String description = asset.getDescription() != null ? asset.getDescription() : "";
        return new AbilityDefinition(asset.getId(), asset.getType(), defaultValue,
                asset.getMin(), asset.getMax(), description);
    }

    @Nullable
    private static AbilityAsset asset(@Nonnull String id) {
        DefaultAssetMap<String, AbilityAsset> map = assetMap();
        return map != null ? map.getAsset(id) : null;
    }

    @Nullable
    private static DefaultAssetMap<String, AbilityAsset> assetMap() {
        try {
            return AbilityAsset.getAssetMap();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
