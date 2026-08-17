package com.riprod.abilityapi.builtin.flight;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.abilityapi.core.asset.AbilityHandlerConfig;
import javax.annotation.Nonnull;

public final class FlightConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final FlightConfig DEFAULTS = new FlightConfig();

    @Nonnull
    public static final BuilderCodec<FlightConfig> CODEC = BuilderCodec
            .builder(FlightConfig.class, FlightConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("ReassertSeconds", Codec.FLOAT),
                    (config, v) -> config.reassertSeconds = v,
                    config -> config.reassertSeconds)
            .documentation("How often this re-evaluates. Governs both repair after another system "
                    + "resets movement settings and how quickly conditional flight reacts.")
            .addValidator(Validators.min(0.1f))
            .add()
            .build();

    private float reassertSeconds = 15.0f;

    public float getReassertSeconds() {
        return this.reassertSeconds;
    }
}
