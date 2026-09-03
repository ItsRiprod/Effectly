package com.riprod.effectly.builtin.effects.movement;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.riprod.effectly.core.effects.registry.AbilityHandlerConfig;

import javax.annotation.Nonnull;

public final class MovementStateConfig extends AbilityHandlerConfig {

    public enum Field {
        CAN_FLY,
        BASE_SPEED
    }

    @Nonnull
    public static final MovementStateConfig DEFAULTS = new MovementStateConfig();

    @Nonnull
    public static final BuilderCodec<MovementStateConfig> CODEC = BuilderCodec
            .builder(MovementStateConfig.class, MovementStateConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("Field", new EnumCodec<>(Field.class)),
                    (config, v) -> config.field = v,
                    config -> config.field)
            .documentation("Movement setting this ability contributes to. CanFly is true when any "
                    + "contributing ability is active; BaseSpeed multiplies every active contribution")
            .add()
            .build();

    private Field field = Field.BASE_SPEED;

    @Nonnull
    public Field getField() {
        return this.field;
    }
}
