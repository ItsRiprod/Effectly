package com.riprod.effectly.builtin.effects.thorns;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.riprod.effectly.core.effects.registry.AbilityHandlerConfig;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

public final class ThornsConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final ThornsConfig DEFAULTS = new ThornsConfig();

    @Nonnull
    public static final BuilderCodec<@NotNull ThornsConfig> CODEC = BuilderCodec
            .builder(ThornsConfig.class, ThornsConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("DamageCause", Codec.STRING),
                    (config, v) -> config.damageCause = v,
                    config -> config.damageCause)
            .documentation("DamageCause asset id the reflected damage is dealt as. Damage arriving "
                    + "with this cause is ignored by every thorns ability, which is what stops two "
                    + "thorns wearers bouncing damage off each other")
            .addValidator(Validators.nonEmptyString())
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    private String damageCause = "Effectly_Thorns";

    @Nonnull
    public String getDamageCause() {
        return this.damageCause;
    }
}
