package com.riprod.effectly.builtin.conditions.liquid;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.riprod.effectly.core.conditions.registry.ConditionConfig;

import javax.annotation.Nonnull;

public final class InLiquidConfig extends ConditionConfig {

    public enum State {
        IN_FLUID,
        SWIMMING
    }

    @Nonnull
    public static final InLiquidConfig DEFAULTS = new InLiquidConfig();

    @Nonnull
    public static final BuilderCodec<InLiquidConfig> CODEC = BuilderCodec
            .builder(InLiquidConfig.class, InLiquidConfig::new, ConditionConfig.BASE_CODEC)
            .append(new KeyedCodec<>("State", new EnumCodec<>(State.class)),
                    (config, v) -> config.state = v,
                    config -> config.state)
            .documentation("InFluid passes while touching liquid at all; Swimming passes only while "
                    + "actually swimming in it")
            .add()
            .build();

    private State state = State.IN_FLUID;

    @Nonnull
    public State getState() {
        return this.state;
    }
}
