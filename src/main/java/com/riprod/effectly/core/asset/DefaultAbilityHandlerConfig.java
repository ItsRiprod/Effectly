package com.riprod.effectly.core.asset;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import javax.annotation.Nonnull;

public final class DefaultAbilityHandlerConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final String TYPE = "Default";

    @Nonnull
    public static final BuilderCodec<DefaultAbilityHandlerConfig> CODEC = BuilderCodec
            .builder(DefaultAbilityHandlerConfig.class, DefaultAbilityHandlerConfig::new,
                    AbilityHandlerConfig.BASE_CODEC)
            .build();

    public static void register() {
        AbilityHandlerConfig.CODEC.register(
                Priority.DEFAULT, TYPE, DefaultAbilityHandlerConfig.class, CODEC);
    }
}
