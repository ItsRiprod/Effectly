package com.riprod.effectly.builtin.effects.healthregen;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.effectly.core.effects.registry.AbilityHandlerConfig;

import javax.annotation.Nonnull;

public final class HealthRegenConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final HealthRegenConfig DEFAULTS = new HealthRegenConfig();

    @Nonnull
    public static final BuilderCodec<HealthRegenConfig> CODEC = BuilderCodec
            .builder(HealthRegenConfig.class, HealthRegenConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("DelaySeconds", Codec.FLOAT),
                    (config, v) -> config.delaySeconds = v,
                    config -> config.delaySeconds)
            .documentation("Seconds after taking damage before regen resumes, mirroring the base-game "
                    + "stamina regen delay feel.")
            .addValidator(Validators.min(0.0f))
            .add()
            .build();

    private float delaySeconds = 5.0f;

    public float getDelaySeconds() {
        return this.delaySeconds;
    }
}
