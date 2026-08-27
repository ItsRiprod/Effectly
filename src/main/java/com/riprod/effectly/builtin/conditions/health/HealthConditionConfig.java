package com.riprod.effectly.builtin.conditions.health;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.effectly.core.conditions.registry.ConditionConfig;

import javax.annotation.Nonnull;

public final class HealthConditionConfig extends ConditionConfig {

    public enum Target {
        SELF,
        DAMAGE_TARGET
    }

    public enum Comparison {
        BELOW,
        AT_OR_ABOVE
    }

    @Nonnull
    public static final HealthConditionConfig DEFAULTS = new HealthConditionConfig();

    @Nonnull
    public static final BuilderCodec<HealthConditionConfig> CODEC = BuilderCodec
            .builder(HealthConditionConfig.class, HealthConditionConfig::new,
                    ConditionConfig.BASE_CODEC)
            .append(new KeyedCodec<>("Target", new EnumCodec<>(Target.class)),
                    (config, v) -> config.target = v,
                    config -> config.target)
            .documentation("Whose health to read. Self is the ability holder; DamageTarget is the other "
                    + "entity in a damage context and never passes outside one")
            .add()
            .append(new KeyedCodec<>("Comparison", new EnumCodec<>(Comparison.class)),
                    (config, v) -> config.comparison = v,
                    config -> config.comparison)
            .documentation("Below passes under the threshold; AtOrAbove passes at or over it")
            .add()
            .append(new KeyedCodec<>("Threshold", Codec.INTEGER),
                    (config, v) -> config.threshold = v,
                    config -> config.threshold)
            .documentation("Health percentage (0-100) to compare against. A grant may override this")
            .addValidator(Validators.range(0, 100))
            .add()
            .build();

    private Target target = Target.SELF;
    private Comparison comparison = Comparison.BELOW;
    private int threshold = 50;

    @Nonnull
    public Target getTarget() {
        return this.target;
    }

    @Nonnull
    public Comparison getComparison() {
        return this.comparison;
    }

    public int getThreshold() {
        return this.threshold;
    }
}
