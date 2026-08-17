package com.riprod.abilityapi.core.condition;

import com.riprod.abilityapi.ability.AbilityConditionSpec;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    }

    @Nullable
    public static AbilityCondition get(@Nonnull String id) {
        return CONDITIONS.get(id);
    }

    @Nonnull
    public static Collection<AbilityCondition> all() {
        return Collections.unmodifiableCollection(CONDITIONS.values());
    }

    @Nullable
    public static AbilityCondition byKeyword(@Nonnull String keyword) {
        AbilityCondition byId = CONDITIONS.get(keyword);
        if (byId != null) return byId;
        for (AbilityCondition condition : CONDITIONS.values()) {
            if (condition.keyword().equals(keyword)) return condition;
        }
        return null;
    }

    @Nonnull
    public static List<AbilityConditionSpec> parse(@Nonnull String[] tokens) {
        List<AbilityConditionSpec> out = new ArrayList<>();
        int i = 0;
        while (i < tokens.length) {
            AbilityCondition condition = byKeyword(tokens[i].toLowerCase(Locale.ROOT));
            if (condition == null) {
                i++;
                continue;
            }
            String[] remaining = Arrays.copyOfRange(tokens, i + 1, tokens.length);
            AbilityCondition.Parsed parsed = condition.parse(remaining);
            if (parsed == null) {
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
        AbilityCondition condition = CONDITIONS.get(spec.type());
        if (condition == null) {
            LOGGER.at(Level.FINE).log("Unknown condition type '%s' -> false", spec.type());
            return false;
        }
        return condition.test(context, spec);
    }

    @Nonnull
    public static String describe(@Nonnull AbilityConditionSpec spec) {
        AbilityCondition condition = CONDITIONS.get(spec.type());
        return condition != null ? condition.describe(spec) : spec.type() + "=" + spec.param();
    }
}
