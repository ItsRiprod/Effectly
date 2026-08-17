package com.riprod.abilityapi.core.stat;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public final class StatAccumulator {

    private float baseSpeedMultiplier = 1.0f;
    private final Map<Integer, Float> maxStatAdditions = new LinkedHashMap<>();

    public void multiplyBaseSpeed(float multiplier) {
        if (multiplier > 0f) baseSpeedMultiplier *= multiplier;
    }

    public void addMaxStat(int statIndex, float amount) {
        if (amount == 0f) return;
        maxStatAdditions.merge(statIndex, amount, Float::sum);
    }

    public float getBaseSpeedMultiplier() {
        return baseSpeedMultiplier;
    }

    @Nonnull
    public Map<Integer, Float> getMaxStatAdditions() {
        return maxStatAdditions;
    }
}
