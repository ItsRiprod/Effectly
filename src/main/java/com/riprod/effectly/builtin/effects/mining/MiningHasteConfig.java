package com.riprod.effectly.builtin.effects.mining;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.effectly.core.asset.AbilityHandlerConfig;

import javax.annotation.Nonnull;

public final class MiningHasteConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final MiningHasteConfig DEFAULTS = new MiningHasteConfig();

    @Nonnull
    public static final BuilderCodec<MiningHasteConfig> CODEC = BuilderCodec
            .builder(MiningHasteConfig.class, MiningHasteConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("PerLevel", Codec.DOUBLE),
                    (config, v) -> config.perLevel = v,
                    config -> config.perLevel)
            .documentation("Extra block-breaking damage per level. Block damage is multiplied by "
                    + "(1 + this * level), so 0.2 at level 5 is 2x.")
            .addValidator(Validators.min(0.0))
            .add()
            .build();

    private double perLevel = 0.2;

    public double getPerLevel() {
        return this.perLevel;
    }
}
