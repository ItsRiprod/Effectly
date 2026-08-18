package com.riprod.effectly.builtin.effects.itemmagnet;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.effectly.core.asset.AbilityHandlerConfig;

import javax.annotation.Nonnull;

public final class ItemMagnetConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final ItemMagnetConfig DEFAULTS = new ItemMagnetConfig();

    @Nonnull
    public static final BuilderCodec<ItemMagnetConfig> CODEC = BuilderCodec
            .builder(ItemMagnetConfig.class, ItemMagnetConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("BaseRange", Codec.DOUBLE),
                    (config, v) -> config.baseRange = v,
                    config -> config.baseRange)
            .documentation("Magnet radius in blocks at an ability value of 1.0. Effective radius is "
                    + "this multiplied by the ability value.")
            .addValidator(Validators.min(0.0))
            .add()
            .append(new KeyedCodec<>("LerpSpeed", Codec.DOUBLE),
                    (config, v) -> config.lerpSpeed = v,
                    config -> config.lerpSpeed)
            .documentation("Blocks per second that a magnetised item travels toward the player.")
            .addValidator(Validators.min(0.0))
            .add()
            .append(new KeyedCodec<>("PickupThreshold", Codec.DOUBLE),
                    (config, v) -> config.pickupThreshold = v,
                    config -> config.pickupThreshold)
            .documentation("Distance at which the magnet stops pulling so normal pickup takes over.")
            .addValidator(Validators.min(0.0))
            .add()
            .build();

    private double baseRange = 8.0;
    private double lerpSpeed = 3.0;
    private double pickupThreshold = 0.5;

    public double getBaseRange() {
        return this.baseRange;
    }

    public double getLerpSpeed() {
        return this.lerpSpeed;
    }

    public double getPickupThreshold() {
        return this.pickupThreshold;
    }
}
