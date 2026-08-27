package com.riprod.effectly.builtin.effects.secondchance;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.effectly.core.effects.registry.EffectHandlerConfig;

import javax.annotation.Nonnull;

public final class SecondChanceConfig extends EffectHandlerConfig {

    @Nonnull
    public static final SecondChanceConfig DEFAULTS = new SecondChanceConfig();

    @Nonnull
    public static final BuilderCodec<SecondChanceConfig> CODEC = BuilderCodec
            .builder(SecondChanceConfig.class, SecondChanceConfig::new, EffectHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("RestorePercent", Codec.FLOAT),
                    (config, v) -> config.restorePercent = v,
                    config -> config.restorePercent)
            .documentation("Fraction of max health the player is restored to when this saves them "
                    + "from lethal damage.")
            .addValidator(Validators.range(0.0f, 1.0f))
            .add()
            .append(new KeyedCodec<>("CooldownSeconds", Codec.FLOAT),
                    (config, v) -> config.cooldownSeconds = v,
                    config -> config.cooldownSeconds)
            .documentation("Cooldown before this can trigger again for the same player.")
            .addValidator(Validators.min(0.0f))
            .add()
            .build();

    private float restorePercent = 0.2f;
    private float cooldownSeconds = 300.0f;

    public float getRestorePercent() {
        return this.restorePercent;
    }

    public float getCooldownSeconds() {
        return this.cooldownSeconds;
    }
}
