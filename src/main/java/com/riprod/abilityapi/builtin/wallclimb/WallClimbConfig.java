package com.riprod.abilityapi.builtin.wallclimb;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.abilityapi.core.asset.AbilityHandlerConfig;
import javax.annotation.Nonnull;

public final class WallClimbConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final WallClimbConfig DEFAULTS = new WallClimbConfig();

    @Nonnull
    public static final BuilderCodec<WallClimbConfig> CODEC = BuilderCodec
            .builder(WallClimbConfig.class, WallClimbConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("ProbeDistance", Codec.DOUBLE),
                    (config, v) -> config.probeDistance = v,
                    config -> config.probeDistance)
            .documentation("How far in front of the player to probe for a solid wall, in blocks.")
            .addValidator(Validators.min(0.0))
            .add()
            .append(new KeyedCodec<>("Velocity", Codec.DOUBLE),
                    (config, v) -> config.velocity = v,
                    config -> config.velocity)
            .documentation("Base upward velocity applied while climbing a wall.")
            .add()
            .append(new KeyedCodec<>("VelocityUp", Codec.DOUBLE),
                    (config, v) -> config.velocityUp = v,
                    config -> config.velocityUp)
            .documentation("Extra upward velocity added while the jump key is held.")
            .add()
            .append(new KeyedCodec<>("VelocityDown", Codec.DOUBLE),
                    (config, v) -> config.velocityDown = v,
                    config -> config.velocityDown)
            .documentation("Velocity subtracted while the crouch key is held, to climb down.")
            .add()
            .build();

    private double probeDistance = 0.6;
    private double velocity = 1.2;
    private double velocityUp = 0.2;
    private double velocityDown = 0.5;

    public double getProbeDistance() {
        return this.probeDistance;
    }

    public double getVelocity() {
        return this.velocity;
    }

    public double getVelocityUp() {
        return this.velocityUp;
    }

    public double getVelocityDown() {
        return this.velocityDown;
    }
}
