package com.riprod.effectly.core.condition.asset;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import javax.annotation.Nonnull;

public final class DefaultAbilityConditionConfig extends AbilityConditionConfig {

    @Nonnull
    public static final String TYPE = "Default";

    @Nonnull
    public static final BuilderCodec<DefaultAbilityConditionConfig> CODEC = BuilderCodec
            .builder(DefaultAbilityConditionConfig.class, DefaultAbilityConditionConfig::new,
                    AbilityConditionConfig.BASE_CODEC)
            .build();

    public static void register() {
        AbilityConditionConfig.CODEC.register(
                Priority.DEFAULT, TYPE, DefaultAbilityConditionConfig.class, CODEC);
    }
}
