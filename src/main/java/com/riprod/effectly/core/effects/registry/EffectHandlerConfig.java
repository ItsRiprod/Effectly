package com.riprod.effectly.core.effects.registry;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class EffectHandlerConfig {

    @Nonnull
    public static final CodecMapCodec<EffectHandlerConfig> CODEC = new CodecMapCodec<>("Id", true, false);

    @Nonnull
    public static final BuilderCodec<EffectHandlerConfig> BASE_CODEC = BuilderCodec
            .abstractBuilder(EffectHandlerConfig.class)
            .append(new KeyedCodec<>("Id", Codec.STRING),
                    (config, v) -> config.id = v,
                    config -> config.id)
            .metadata(new UIEditor(new UIEditor.Dropdown("EffectlyHandlers")))
            .addValidatorLate(() -> EffectHandlerKeyValidator.INSTANCE.late())
            .documentation("Registered ability handler that implements this ability - several ability "
                    + "assets may share one handler. Defaults to the asset id.")
            .add()
            .build();

    protected String id;

    @Nullable
    public String getId() {
        return this.id;
    }
}
