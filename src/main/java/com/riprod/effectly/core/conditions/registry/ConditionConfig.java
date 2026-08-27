package com.riprod.effectly.core.conditions.registry;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ConditionConfig {

    @Nonnull
    public static final CodecMapCodec<ConditionConfig> CODEC = new CodecMapCodec<>("Id", true, false);

    @Nonnull
    public static final BuilderCodec<ConditionConfig> BASE_CODEC = BuilderCodec
            .abstractBuilder(ConditionConfig.class)
            .append(new KeyedCodec<>("Id", Codec.STRING),
                    (config, v) -> config.id = v,
                    config -> config.id)
            .metadata(new UIEditor(new UIEditor.Dropdown("EffectlyConditions")))
            .addValidatorLate(() -> ConditionKeyValidator.INSTANCE.late())
            .documentation("Registered condition handler that implements this condition - several "
                    + "condition assets may share one handler. Defaults to the asset id.")
            .add()
            .build();

    protected String id;

    @Nullable
    public String getId() {
        return this.id;
    }
}
