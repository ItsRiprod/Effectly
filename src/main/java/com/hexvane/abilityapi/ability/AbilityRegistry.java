package com.hexvane.abilityapi.ability;

import com.hexvane.abilityapi.core.asset.AbilityAsset;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityRegistry {
    private static final Map<String, AbilityDefinition> REGISTRY = new HashMap<>();

    private AbilityRegistry() {}

    public static void register(@Nonnull AbilityDefinition def) {
        REGISTRY.put(def.id(), def);
    }

    @Nullable
    public static AbilityDefinition get(@Nonnull String id) {
        AbilityDefinition programmatic = REGISTRY.get(id);
        if (programmatic != null) return programmatic;

        AbilityAsset asset = asset(id);
        return asset != null ? toDefinition(asset) : null;
    }

    public static boolean isValid(@Nonnull String id) {
        return get(id) != null;
    }

    @Nonnull
    public static Set<String> getAllIds() {
        Set<String> ids = new LinkedHashSet<>(REGISTRY.keySet());
        DefaultAssetMap<String, AbilityAsset> map = assetMap();
        if (map != null) {
            ids.addAll(map.getAssetMap().keySet());
        }
        return ids;
    }

    public static void clear() {
        REGISTRY.clear();
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
