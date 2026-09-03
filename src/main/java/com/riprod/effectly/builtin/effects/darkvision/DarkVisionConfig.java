package com.riprod.effectly.builtin.effects.darkvision;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.effectly.core.effects.registry.AbilityHandlerConfig;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

public final class DarkVisionConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final DarkVisionConfig DEFAULTS = new DarkVisionConfig();

    @Nonnull
    public static final BuilderCodec<@NotNull DarkVisionConfig> CODEC = BuilderCodec
            .builder(DarkVisionConfig.class, DarkVisionConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("EffectId", Codec.STRING),
                    (config, v) -> config.effectId = v,
                    config -> config.effectId)
            .documentation("EntityEffect asset id applied by this ability. Override to ship your own "
                    + "screen effect without replacing the built-in asset.")
            .addValidator(Validators.nonEmptyString())
            .add()
            .append(new KeyedCodec<>("CheckSeconds", Codec.FLOAT),
                    (config, v) -> config.checkSeconds = v,
                    config -> config.checkSeconds)
            .documentation("How often this re-evaluates its conditions and renews its screen effect.")
            .addValidator(Validators.min(0.1f))
            .add()
            .build();

    private String effectId = "Effectly_Dark_Vision";
    private float checkSeconds = 1.0f;

    @Nonnull
    public String getEffectId() {
        return this.effectId;
    }

    public float getCheckSeconds() {
        return this.checkSeconds;
    }
}
