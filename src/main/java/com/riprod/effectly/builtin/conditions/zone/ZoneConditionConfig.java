package com.riprod.effectly.builtin.conditions.zone;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.effectly.core.conditions.registry.ConditionConfig;

import javax.annotation.Nonnull;

public final class ZoneConditionConfig extends ConditionConfig {

    @Nonnull
    public static final ZoneConditionConfig DEFAULTS = new ZoneConditionConfig();

    @Nonnull
    public static final BuilderCodec<ZoneConditionConfig> CODEC = BuilderCodec
            .builder(ZoneConditionConfig.class, ZoneConditionConfig::new, ConditionConfig.BASE_CODEC)
            .append(new KeyedCodec<>("Zones", Codec.INT_ARRAY),
                    (config, v) -> config.zones = v,
                    config -> config.zones)
            .documentation("World zone ids this condition accepts. A grant may override these with "
                    + "its own zone ids")
            .add()
            .build();

    private static final int[] EMPTY = new int[0];

    private int[] zones;

    @Nonnull
    public int[] getZones() {
        return this.zones == null ? EMPTY : this.zones;
    }
}
