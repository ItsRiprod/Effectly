package com.riprod.abilityapi.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.configly.Config;
import com.riprod.configly.Configly;
import javax.annotation.Nonnull;

public final class AbilityApiConfig extends Config {

    @Nonnull
    public static final String TYPE = "AbilityAPI";

    @Nonnull
    public static final AbilityApiConfig DEFAULTS = new AbilityApiConfig();

    @Nonnull
    public static final BuilderCodec<AbilityApiConfig> CODEC = BuilderCodec
            .builder(AbilityApiConfig.class, AbilityApiConfig::new)
            .append(new KeyedCodec<>("StatReassertSeconds", Codec.FLOAT),
                    (config, v) -> config.statReassertSeconds = v,
                    config -> config.statReassertSeconds)
            .documentation("How often the shared stat system re-applies stat-backed abilities. Applies "
                    + "to every stat ability at once, so it is not owned by any one of them.")
            .addValidator(Validators.min(0.1f))
            .add()
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

    private float statReassertSeconds = 1.0f;
    private double sunlightMinFactor = 0.2;
    private int sunlightMinEffective = 10;

    private AbilityApiConfig() {
    }

    @Nonnull
    public static AbilityApiConfig get() {
        return Configly.getOrElse(TYPE, AbilityApiConfig.class, DEFAULTS);
    }

    public float getStatReassertSeconds() {
        return statReassertSeconds;
    }

    public double getSunlightMinFactor() {
        return sunlightMinFactor;
    }

    public int getSunlightMinEffective() {
        return sunlightMinEffective;
    }
}
