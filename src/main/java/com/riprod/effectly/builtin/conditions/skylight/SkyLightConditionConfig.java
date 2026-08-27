package com.riprod.effectly.builtin.conditions.skylight;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.effectly.core.conditions.registry.ConditionConfig;

import javax.annotation.Nonnull;

public final class SkyLightConditionConfig extends ConditionConfig {

    @Nonnull
    public static final SkyLightConditionConfig DEFAULTS = new SkyLightConditionConfig();

    @Nonnull
    public static final BuilderCodec<SkyLightConditionConfig> CODEC = BuilderCodec
            .builder(SkyLightConditionConfig.class, SkyLightConditionConfig::new,
                    ConditionConfig.BASE_CODEC)
            .append(new KeyedCodec<>("MinSunlightFactor", Codec.DOUBLE),
                    (config, v) -> config.minSunlightFactor = v,
                    config -> config.minSunlightFactor)
            .documentation("Lowest world sunlight factor (0-1) that still passes. Raise it to require daytime")
            .addValidator(Validators.range(0.0, 1.0))
            .add()
            .append(new KeyedCodec<>("MaxSunlightFactor", Codec.DOUBLE),
                    (config, v) -> config.maxSunlightFactor = v,
                    config -> config.maxSunlightFactor)
            .documentation("Highest world sunlight factor (0-1) that still passes. Lower it to require night")
            .addValidator(Validators.range(0.0, 1.0))
            .add()
            .append(new KeyedCodec<>("MinSkyLight", Codec.INTEGER),
                    (config, v) -> config.minSkyLight = v,
                    config -> config.minSkyLight)
            .documentation("Lowest raw sky light (0-15) at the entity. Raise it to require open sky")
            .addValidator(Validators.range(0, 15))
            .add()
            .append(new KeyedCodec<>("MaxSkyLight", Codec.INTEGER),
                    (config, v) -> config.maxSkyLight = v,
                    config -> config.maxSkyLight)
            .documentation("Highest raw sky light (0-15) at the entity. Lower it to require cover")
            .addValidator(Validators.range(0, 15))
            .add()
            .append(new KeyedCodec<>("MinEffectiveLight", Codec.INTEGER),
                    (config, v) -> config.minEffectiveLight = v,
                    config -> config.minEffectiveLight)
            .documentation("Lowest sun-scaled light (sky light multiplied by the sunlight factor, 0-15) "
                    + "that still passes")
            .addValidator(Validators.range(0, 15))
            .add()
            .build();

    private double minSunlightFactor = 0.0;
    private double maxSunlightFactor = 1.0;
    private int minSkyLight = 0;
    private int maxSkyLight = 15;
    private int minEffectiveLight = 0;

    public double getMinSunlightFactor() {
        return this.minSunlightFactor;
    }

    public double getMaxSunlightFactor() {
        return this.maxSunlightFactor;
    }

    public int getMinSkyLight() {
        return this.minSkyLight;
    }

    public int getMaxSkyLight() {
        return this.maxSkyLight;
    }

    public int getMinEffectiveLight() {
        return this.minEffectiveLight;
    }
}
