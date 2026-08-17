package com.riprod.abilityapi.builtin.oxygen;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.abilityapi.core.asset.AbilityHandlerConfig;
import javax.annotation.Nonnull;

public final class OxygenConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final OxygenConfig DEFAULTS = new OxygenConfig();

    @Nonnull
    public static final BuilderCodec<OxygenConfig> CODEC = BuilderCodec
            .builder(OxygenConfig.class, OxygenConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("UnitsPerSecond", Codec.FLOAT),
                    (config, v) -> config.unitsPerSecond = v,
                    config -> config.unitsPerSecond)
            .documentation("Oxygen stat units one point of this ability adds to max breath. Oxygen.json "
                    + "drains 3 per 0.5s while suffocating, so 6 units is one second underwater.")
            .addValidator(Validators.min(0.0f))
            .add()
            .build();

    private float unitsPerSecond = 6.0f;

    public float getUnitsPerSecond() {
        return this.unitsPerSecond;
    }
}
