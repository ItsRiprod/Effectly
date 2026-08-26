package com.riprod.effectly.core.condition;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.effectly.ability.AbilityConditionSpec;
import com.riprod.effectly.core.condition.asset.AbilityConditionAsset;
import com.riprod.effectly.core.condition.asset.AbilityConditionConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityConditions {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final Map<String, AbilityCondition> CONDITIONS = new LinkedHashMap<>();

    private AbilityConditions() {}

    public static void reset() {
        CONDITIONS.clear();
    }

    public static void register(@Nonnull AbilityCondition condition) {
        AbilityCondition existing = CONDITIONS.putIfAbsent(condition.getId(), condition);
        if (existing != null && existing.getClass() != condition.getClass()) {
            throw new IllegalArgumentException("duplicate ability condition id: " + condition.getId());
        }
        if (existing != null) CONDITIONS.put(condition.getId(), condition);

        AbilityCondition.ConfigBinding<? extends AbilityConditionConfig> binding = condition.getConfigBinding();
        if (binding != null) {
            AbilityConditionConfig.CODEC.register(condition.getId(), binding.type(), binding.codec());
        }
    }

    @Nullable
    public static AbilityCondition get(@Nonnull String handlerId) {
        return CONDITIONS.get(handlerId);
    }

    @Nullable
    public static AbilityCondition forCondition(@Nonnull String conditionId) {
        AbilityConditionAsset asset = asset(conditionId);
        if (asset == null) return null;
        return CONDITIONS.get(asset.getHandler());
    }

    @Nonnull
    public static Collection<AbilityCondition> all() {
        return Collections.unmodifiableCollection(CONDITIONS.values());
    }

    @Nonnull
    public static Collection<String> ids() {
        return Collections.unmodifiableCollection(CONDITIONS.keySet());
    }

    @Nonnull
    public static Collection<AbilityConditionAsset> assets() {
        DefaultAssetMap<String, AbilityConditionAsset> map = assetMap();
        if (map == null) return List.of();
        Map<String, AbilityConditionAsset> sorted = new TreeMap<>(map.getAssetMap());
        List<AbilityConditionAsset> out = new ArrayList<>(sorted.size());
        for (AbilityConditionAsset asset : sorted.values()) {
            if (asset.isEnabled()) out.add(asset);
        }
        return out;
    }

    @Nullable
    public static AbilityConditionAsset byKeyword(@Nonnull String keyword) {
        AbilityConditionAsset byId = asset(keyword);
        if (byId != null && byId.isEnabled()) return byId;
        for (AbilityConditionAsset asset : assets()) {
            if (asset.getKeyword().equalsIgnoreCase(keyword)) return asset;
            if (asset.getId().equalsIgnoreCase(keyword)) return asset;
        }
        return null;
    }

    @Nonnull
    public static List<AbilityConditionSpec> parse(@Nonnull String[] tokens) {
        return parse(tokens, null);
    }

    @Nonnull
    public static List<AbilityConditionSpec> parse(
            @Nonnull String[] tokens,
            @Nullable List<String> unparsedOut) {
        List<AbilityConditionSpec> out = new ArrayList<>();
        int i = 0;
        while (i < tokens.length) {
            AbilityConditionAsset asset = byKeyword(tokens[i]);
            AbilityCondition condition = asset != null ? CONDITIONS.get(asset.getHandler()) : null;
            if (condition == null) {
                if (unparsedOut != null) unparsedOut.add(tokens[i]);
                i++;
                continue;
            }
            String[] remaining = Arrays.copyOfRange(tokens, i + 1, tokens.length);
            AbilityCondition.Parsed parsed = condition.parse(asset, remaining);
            if (parsed == null) {
                if (unparsedOut != null) unparsedOut.add(tokens[i]);
                i++;
                continue;
            }
            out.add(parsed.spec());
            i += 1 + parsed.consumed();
        }
        return out;
    }

    public static boolean test(
            @Nonnull AbilityConditionContext context,
            @Nonnull AbilityConditionSpec spec) {
        AbilityConditionAsset asset = asset(spec.type());
        if (asset == null) {
            LOGGER.at(Level.FINE).log("Unknown condition '%s' -> false", spec.type());
            return false;
        }
        if (!asset.isEnabled()) {
            LOGGER.at(Level.FINE).log("Condition '%s' is disabled -> false", spec.type());
            return false;
        }
        AbilityCondition condition = CONDITIONS.get(asset.getHandler());
        if (condition == null) {
            LOGGER.at(Level.FINE).log("No handler '%s' for condition '%s' -> false",
                    asset.getHandler(), spec.type());
            return false;
        }
        return condition.test(context, asset, spec);
    }

    @Nonnull
    public static String describe(@Nonnull AbilityConditionSpec spec) {
        AbilityConditionAsset asset = asset(spec.type());
        AbilityCondition condition = asset != null ? CONDITIONS.get(asset.getHandler()) : null;
        if (asset == null || condition == null) {
            return spec.hasParam() ? spec.type() + "=" + spec.param() : spec.type();
        }
        return condition.describe(asset, spec);
    }

    @Nonnull
    public static String usage(@Nonnull AbilityConditionAsset asset) {
        AbilityCondition condition = CONDITIONS.get(asset.getHandler());
        String arguments = condition != null ? condition.argumentUsage() : "";
        return arguments.isEmpty() ? asset.getKeyword() : asset.getKeyword() + " " + arguments;
    }

    @Nullable
    private static AbilityConditionAsset asset(@Nonnull String conditionId) {
        DefaultAssetMap<String, AbilityConditionAsset> map = assetMap();
        return map != null ? map.getAsset(conditionId) : null;
    }

    @Nullable
    private static DefaultAssetMap<String, AbilityConditionAsset> assetMap() {
        try {
            return AbilityConditionAsset.getAssetMap();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
