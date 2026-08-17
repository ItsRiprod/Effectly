package com.riprod.abilityapi.builtin.conditions;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class HealthPercent {

    public static final float UNKNOWN = Float.NaN;

    private HealthPercent() {}

    public static float of(
            @Nullable Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> components) {
        if (ref == null || !ref.isValid()) return UNKNOWN;

        EntityStatMap statMap = components.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) return UNKNOWN;

        int healthIndex = DefaultEntityStatTypes.getHealth();
        if (healthIndex < 0 || healthIndex >= statMap.size()) return UNKNOWN;

        EntityStatValue healthStat = statMap.get(healthIndex);
        if (healthStat == null) return UNKNOWN;

        float max = healthStat.getMax();
        if (max <= 0f) return UNKNOWN;

        return (healthStat.get() / max) * 100f;
    }

    public static boolean isKnown(float percent) {
        return !Float.isNaN(percent);
    }
}
