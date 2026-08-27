package com.riprod.effectly.core.conditions.registry;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.Priority;

import javax.annotation.Nonnull;

public final class DefaultConditionConfig extends ConditionConfig {

    @Nonnull
    public static final String TYPE = "Default";

    @Nonnull
    public static final BuilderCodec<DefaultConditionConfig> CODEC = BuilderCodec
            .builder(DefaultConditionConfig.class, DefaultConditionConfig::new,
                    ConditionConfig.BASE_CODEC)
            .build();

    public static void register() {
        ConditionConfig.CODEC.register(
                Priority.DEFAULT, TYPE, DefaultConditionConfig.class, CODEC);
    }
}
