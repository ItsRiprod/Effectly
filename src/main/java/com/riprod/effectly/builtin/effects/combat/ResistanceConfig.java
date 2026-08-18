package com.riprod.effectly.builtin.effects.combat;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.riprod.effectly.core.asset.AbilityHandlerConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ResistanceConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final BuilderCodec<ResistanceConfig> CODEC = BuilderCodec
            .builder(ResistanceConfig.class, ResistanceConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("DamageCause", Codec.STRING),
                    (config, v) -> config.damageCause = v,
                    config -> config.damageCause)
            .documentation("DamageCause asset id this ability resists. Value semantics: 0 is normal, "
                    + "0 to 1 reduces damage, -1 to 0 increases it. Also covers every cause derived "
                    + "from this one, so Elemental covers Fire and Ice. When a player holds several "
                    + "resistances along one chain the most specific active one wins - including a "
                    + "value of 0, which is how you carve an exception out of a broader resistance.")
            .addValidator(Validators.nonEmptyString())
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    private String damageCause;

    @Nullable
    public String getDamageCause() {
        return this.damageCause;
    }
}
