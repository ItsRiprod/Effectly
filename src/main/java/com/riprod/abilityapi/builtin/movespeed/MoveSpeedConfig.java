package com.riprod.abilityapi.builtin.movespeed;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.abilityapi.core.asset.AbilityHandlerConfig;
import javax.annotation.Nonnull;

public final class MoveSpeedConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final MoveSpeedConfig DEFAULTS = new MoveSpeedConfig();

    @Nonnull
    public static final BuilderCodec<MoveSpeedConfig> CODEC = BuilderCodec
            .builder(MoveSpeedConfig.class, MoveSpeedConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("ReassertSeconds", Codec.FLOAT),
                    (config, v) -> config.reassertSeconds = v,
                    config -> config.reassertSeconds)
            .documentation("How often this re-evaluates. Governs both repair after another system "
                    + "resets movement settings and how quickly conditional speed reacts.")
            .addValidator(Validators.min(0.1f))
            .add()
            .build();

    private float reassertSeconds = 1.0f;

    public float getReassertSeconds() {
        return this.reassertSeconds;
    }
}
