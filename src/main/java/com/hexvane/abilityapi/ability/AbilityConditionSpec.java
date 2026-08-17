package com.hexvane.abilityapi.ability;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityConditionSpec {
    public static final String TYPE_IN_ZONE = "in_zone";
    public static final String TYPE_HEALTH_BELOW = "health_below";
    public static final String TYPE_HEALTH_ABOVE = "health_above";
    public static final String TYPE_TARGET_HEALTH_BELOW = "target_health_below";
    public static final String TYPE_TARGET_HEALTH_ABOVE = "target_health_above";
    public static final String TYPE_IN_SUNLIGHT = "in_sunlight";

    @Nonnull
    public static final BuilderCodec<AbilityConditionSpec> CODEC = BuilderCodec
            .builder(AbilityConditionSpec.class, AbilityConditionSpec::new)
            .append(new KeyedCodec<>("Type", Codec.STRING),
                    (spec, v) -> spec.type = v,
                    spec -> spec.type)
            .documentation("Condition type: in_zone, in_sunlight, health_below, health_above, "
                    + "target_health_below or target_health_above")
            .add()
            .append(new KeyedCodec<>("Param", Codec.INTEGER),
                    (spec, v) -> spec.param = v,
                    spec -> spec.param)
            .documentation("Single numeric parameter: a health percentage (0-100), or the sole zone id "
                    + "when ZoneIds is absent")
            .add()
            .append(new KeyedCodec<>("ZoneIds", Codec.INT_ARRAY),
                    (spec, v) -> spec.zoneIds = toList(v),
                    spec -> toArray(spec.zoneIds))
            .documentation("Additional allowed zone ids for in_zone; when absent, Param is the only allowed zone")
            .add()
            .build();

    private String type;
    private int param;
    @Nullable
    private List<Integer> zoneIds;

    private AbilityConditionSpec() {
    }

    public AbilityConditionSpec(@Nonnull String type, int param, @Nullable List<Integer> zoneIds) {
        this.type = type;
        this.param = param;
        this.zoneIds = zoneIds;
    }

    public AbilityConditionSpec(@Nonnull String type, int param) {
        this(type, param, null);
    }

    @Nonnull
    public String type() {
        return type;
    }

    public int param() {
        return param;
    }

    @Nullable
    public List<Integer> zoneIds() {
        return zoneIds;
    }

    @Nonnull
    public List<Integer> allowedZoneIds() {
        if (zoneIds != null && !zoneIds.isEmpty()) return zoneIds;
        return Collections.singletonList(param);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AbilityConditionSpec spec)) return false;
        return param == spec.param && Objects.equals(type, spec.type) && Objects.equals(zoneIds, spec.zoneIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, param, zoneIds);
    }

    @Override
    public String toString() {
        return "AbilityConditionSpec[type=" + type + ", param=" + param + ", zoneIds=" + zoneIds + "]";
    }

    @Nullable
    private static List<Integer> toList(@Nullable int[] values) {
        if (values == null || values.length == 0) return null;
        List<Integer> list = new ArrayList<>(values.length);
        for (int value : values) list.add(value);
        return list;
    }

    @Nullable
    private static int[] toArray(@Nullable List<Integer> values) {
        if (values == null || values.isEmpty()) return null;
        int[] out = new int[values.size()];
        for (int i = 0; i < values.size(); i++) out[i] = values.get(i);
        return out;
    }
}
