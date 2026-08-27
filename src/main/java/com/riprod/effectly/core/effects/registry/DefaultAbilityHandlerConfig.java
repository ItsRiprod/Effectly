package com.riprod.effectly.core.effects.registry;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.Priority;

import javax.annotation.Nonnull;

public final class DefaultAbilityHandlerConfig extends EffectHandlerConfig {

    @Nonnull
    public static final String TYPE = "Default";

    @Nonnull
    public static final BuilderCodec<DefaultAbilityHandlerConfig> CODEC = BuilderCodec
            .builder(DefaultAbilityHandlerConfig.class, DefaultAbilityHandlerConfig::new,
                    EffectHandlerConfig.BASE_CODEC)
            .build();

    public static void register() {
        EffectHandlerConfig.CODEC.register(
                Priority.DEFAULT, TYPE, DefaultAbilityHandlerConfig.class, CODEC);
    }
}
