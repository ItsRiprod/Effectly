package com.riprod.effectly.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.configly.Config;
import com.riprod.configly.Configly;
import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

public final class EffectlyConfig extends Config {

    @Nonnull
    public static final String TYPE = "Effectly";

    @Nonnull
    public static final EffectlyConfig DEFAULTS = new EffectlyConfig();

    @Nonnull
    public static final BuilderCodec<@NotNull EffectlyConfig> CODEC = BuilderCodec
            .builder(EffectlyConfig.class, EffectlyConfig::new)
            .append(new KeyedCodec<>("SunlightMinFactor", Codec.DOUBLE),
                    (config, v) -> config.sunlightMinFactor = v,
                    config -> config.sunlightMinFactor)
            .documentation("Minimum world sunlight factor (0-1) for the in_sunlight condition to count "
                    + "as daytime. Below this it is treated as night.")
            .addValidator(Validators.range(0.0, 1.0))
            .add()
            .append(new KeyedCodec<>("SunlightMinEffective", Codec.INTEGER),
                    (config, v) -> config.sunlightMinEffective = v,
                    config -> config.sunlightMinEffective)
            .documentation("Minimum effective sunlight (skyLight * sunlightFactor, 0-15 scale) for the "
                    + "in_sunlight condition to pass.")
            .addValidator(Validators.range(0, 15))
            .add()
            .build();

    private double sunlightMinFactor = 0.2;
    private int sunlightMinEffective = 10;

    private EffectlyConfig() {
    }

    @Nonnull
    public static EffectlyConfig get() {
        return Configly.getOrElse(TYPE, EffectlyConfig.class, DEFAULTS);
    }

    public double getSunlightMinFactor() {
        return sunlightMinFactor;
    }

    public int getSunlightMinEffective() {
        return sunlightMinEffective;
    }
}
