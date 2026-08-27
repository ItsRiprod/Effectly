package com.riprod.effectly.builtin.conditions.health;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;
import com.riprod.effectly.core.conditions.ConditionContext;
import com.riprod.effectly.core.conditions.registry.ConditionAsset;
import com.riprod.effectly.core.conditions.registry.ConditionHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class HealthCondition implements ConditionHandler {

    public static final String ID = "health";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<HealthConditionConfig> getConfigBinding() {
        return ConfigBinding.of(HealthConditionConfig.class, HealthConditionConfig.CODEC);
    }

    @Override
    public boolean test(
            @Nonnull ConditionContext context,
            @Nonnull ConditionAsset asset,
            @Nonnull AbilityConditionSpec spec) {
        HealthConditionConfig config =
                asset.configOrDefault(HealthConditionConfig.class, HealthConditionConfig.DEFAULTS);

        Ref<EntityStore> ref = config.getTarget() == HealthConditionConfig.Target.DAMAGE_TARGET
                ? context.getTargetRef()
                : context.getRef();

        float percent = HealthPercent.of(ref, context.getComponents());
        if (!HealthPercent.isKnown(percent)) return false;

        int threshold = spec.paramOrDefault(config.getThreshold());
        return config.getComparison() == HealthConditionConfig.Comparison.BELOW
                ? percent < threshold
                : percent >= threshold;
    }

    @Nullable
    @Override
    public Parsed parse(@Nonnull ConditionAsset asset, @Nonnull String[] remaining) {
        if (remaining.length < 1) {
            return new Parsed(new AbilityConditionSpec(asset.getId()), 0);
        }
        try {
            int percent = Integer.parseInt(remaining[0]);
            if (percent < 0 || percent > 100) return null;
            return new Parsed(new AbilityConditionSpec(asset.getId(), percent), 1);
        } catch (NumberFormatException e) {
            return new Parsed(new AbilityConditionSpec(asset.getId()), 0);
        }
    }

    @Nonnull
    @Override
    public String describe(@Nonnull ConditionAsset asset, @Nonnull AbilityConditionSpec spec) {
        HealthConditionConfig config =
                asset.configOrDefault(HealthConditionConfig.class, HealthConditionConfig.DEFAULTS);
        return asset.getKeyword() + "=" + spec.paramOrDefault(config.getThreshold());
    }

    @Nonnull
    @Override
    public String argumentUsage() {
        return "[percent 0-100]";
    }
}
