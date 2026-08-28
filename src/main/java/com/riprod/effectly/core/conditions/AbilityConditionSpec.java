package com.riprod.effectly.core.conditions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public final class AbilityConditionSpec {

    @Nonnull
    public static final BuilderCodec<@NotNull AbilityConditionSpec> CODEC = BuilderCodec
            .builder(AbilityConditionSpec.class, AbilityConditionSpec::new)
            .append(new KeyedCodec<>("Type", Codec.STRING),
                    (spec, v) -> spec.type = v,
                    spec -> spec.type)
            .documentation("Condition asset id, from Effectly/Conditions")
            .add()
            .append(new KeyedCodec<>("Param", Codec.INTEGER),
                    (spec, v) -> spec.param = v,
                    spec -> spec.param)
            .documentation("Optional numeric override for the condition asset's configured value: a "
                    + "health percentage (0-100), or the sole zone id when ZoneIds is absent. When "
                    + "omitted the asset's own configuration is used")
            .add()
            .append(new KeyedCodec<>("ZoneIds", Codec.INT_ARRAY),
                    (spec, v) -> spec.zoneIds = (v == null || v.length == 0) ? null : v,
                    spec -> spec.zoneIds)
            .documentation("Optional zone id override for in_zone; when absent, Param is the only "
                    + "allowed zone, and when both are absent the asset's configured zones are used")
            .add()
            .build();

    private static final int[] EMPTY = new int[0];

    private String type;
    @Nullable
    private Integer param;
    @Nullable
    private int[] zoneIds;

    private AbilityConditionSpec() {
    }

    public AbilityConditionSpec(@Nonnull String type, @Nullable Integer param, @Nullable List<Integer> zoneIds) {
        this.type = type;
        this.param = param;
        this.zoneIds = toArray(zoneIds);
    }

    public AbilityConditionSpec(@Nonnull String type, int param) {
        this(type, Integer.valueOf(param), null);
    }

    public AbilityConditionSpec(@Nonnull String type) {
        this(type, null, null);
    }

    @Nonnull
    public String type() {
        return type;
    }

    public int param() {
        return param == null ? 0 : param;
    }

    public boolean hasParam() {
        return param != null;
    }

    public int paramOrDefault(int fallback) {
        return param == null ? fallback : param;
    }

    @Nullable
    public List<Integer> zoneIds() {
        return toList(zoneIds);
    }

    /** Hot-path form of {@link #allowedZoneIds()}; empty when neither an override nor a param is set. */
    @Nonnull
    public int[] allowedZoneIdArray() {
        if (zoneIds != null && zoneIds.length > 0) return zoneIds;
        return param == null ? EMPTY : new int[] {param};
    }

    @Nonnull
    public List<Integer> allowedZoneIds() {
        int[] ids = allowedZoneIdArray();
        List<Integer> out = new ArrayList<>(ids.length);
        for (int id : ids) out.add(id);
        return out;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AbilityConditionSpec spec)) return false;
        return Objects.equals(param, spec.param)
                && Objects.equals(type, spec.type)
                && Arrays.equals(zoneIds, spec.zoneIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, param) * 31 + Arrays.hashCode(zoneIds);
    }

    @Override
    public String toString() {
        return "AbilityConditionSpec[type=" + type + ", param=" + param + ", zoneIds=" + Arrays.toString(zoneIds) + "]";
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
