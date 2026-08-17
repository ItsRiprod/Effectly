package com.riprod.abilityapi.builtin.swimspeed;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.abilityapi.core.asset.AbilityHandlerConfig;
import javax.annotation.Nonnull;

public final class SwimSpeedConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final SwimSpeedConfig DEFAULTS = new SwimSpeedConfig();

    @Nonnull
    public static final BuilderCodec<SwimSpeedConfig> CODEC = BuilderCodec
            .builder(SwimSpeedConfig.class, SwimSpeedConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("ReassertSeconds", Codec.FLOAT),
                    (config, v) -> config.reassertSeconds = v,
                    config -> config.reassertSeconds)
            .documentation("How often this re-evaluates while the swimming state is unchanged. Entering "
                    + "or leaving water re-evaluates immediately regardless of this value.")
            .addValidator(Validators.min(0.1f))
            .add()
            .build();

    private float reassertSeconds = 1.0f;

    public float getReassertSeconds() {
        return this.reassertSeconds;
    }
}
