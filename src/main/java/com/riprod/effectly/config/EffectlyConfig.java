package com.riprod.effectly.config;

import com.hypixel.hytale.codec.builder.BuilderCodec;
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
            .build();

    private EffectlyConfig() {
    }

    @Nonnull
    public static EffectlyConfig get() {
        return Configly.getOrElse(TYPE, EffectlyConfig.class, DEFAULTS);
    }
}
