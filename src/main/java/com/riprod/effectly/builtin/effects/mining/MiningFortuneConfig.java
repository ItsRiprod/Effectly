package com.riprod.effectly.builtin.effects.mining;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.riprod.effectly.core.effects.registry.AbilityHandlerConfig;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;

public final class MiningFortuneConfig extends AbilityHandlerConfig {

    @Nonnull
    public static final MiningFortuneConfig DEFAULTS = new MiningFortuneConfig();

    @Nonnull
    public static final BuilderCodec<MiningFortuneConfig> CODEC = BuilderCodec
            .builder(MiningFortuneConfig.class, MiningFortuneConfig::new, AbilityHandlerConfig.BASE_CODEC)
            .append(new KeyedCodec<>("AffectedBlocks", new SetCodec<>(Codec.STRING, LinkedHashSet::new, false)),
                    (config, v) -> config.affectedBlocks = v == null ? new LinkedHashSet<>() : v,
                    config -> config.affectedBlocks)
            .documentation("Block type ids this ability grants extra drops for. Any block not listed "
                    + "here is unaffected no matter how high the ability value is.")
            .add()
            .build();

    private Set<String> affectedBlocks = new LinkedHashSet<>();

    @Nonnull
    public Set<String> getAffectedBlocks() {
        return this.affectedBlocks;
    }
}
