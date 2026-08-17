package com.hexvane.abilityapi.core.asset;

import com.hexvane.abilityapi.ability.AbilityType;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, AbilityAsset>> {

    public static final String ASSET_PATH = "AbilityAPI/Abilities";

    public static final AssetBuilderCodec<String, AbilityAsset> CODEC = buildCodec();

    private static AssetStore<String, AbilityAsset, DefaultAssetMap<String, AbilityAsset>> ASSET_STORE;

    private AssetExtraInfo.Data data;
    private String id;
    private String handlerId;
    private String description;
    private AbilityType type = AbilityType.NUMERIC;
    private double defaultValue = 1.0;
    private double min = 0.0;
    private double max = 1.0;
    private boolean enabled = true;

    private AbilityAsset() {
    }

    public static AssetStore<String, AbilityAsset, DefaultAssetMap<String, AbilityAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(AbilityAsset.class);
        }
        return ASSET_STORE;
    }

    @SuppressWarnings("unchecked")
    public static DefaultAssetMap<String, AbilityAsset> getAssetMap() {
        return (DefaultAssetMap<String, AbilityAsset>) getAssetStore().getAssetMap();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Nonnull
    public String getHandler() {
        return this.handlerId == null ? this.id : this.handlerId;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    @Nonnull
    public AbilityType getType() {
        return this.type;
    }

    public double getDefaultValue() {
        return this.defaultValue;
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    private static AssetBuilderCodec<String, AbilityAsset> buildCodec() {
        return AssetBuilderCodec
                .builder(AbilityAsset.class, AbilityAsset::new, Codec.STRING,
                        (asset, key) -> asset.id = key,
                        asset -> asset.id,
                        (asset, data) -> asset.data = data,
                        asset -> asset.data)
                .append(new KeyedCodec<>("Handler", Codec.STRING),
                        (asset, v) -> asset.handlerId = v,
                        asset -> asset.handlerId)
                .metadata(new UIEditor(new UIEditor.Dropdown("AbilityAPIHandlers")))
                .addValidatorLate(() -> AbilityHandlerKeyValidator.INSTANCE.late())
                .documentation("Registered ability handler that implements this ability - several ability "
                        + "assets may share one handler. Defaults to the asset id.")
                .add()
                .append(new KeyedCodec<>("Type", new EnumCodec<>(AbilityType.class)),
                        (asset, v) -> asset.type = v,
                        asset -> asset.type)
                .documentation("BINARY for on/off abilities, NUMERIC for valued abilities")
                .add()
                .append(new KeyedCodec<>("Default", Codec.DOUBLE),
                        (asset, v) -> asset.defaultValue = v,
                        asset -> asset.defaultValue)
                .documentation("Value used when the ability is granted without an explicit value")
                .add()
                .append(new KeyedCodec<>("Min", Codec.DOUBLE),
                        (asset, v) -> asset.min = v,
                        asset -> asset.min)
                .documentation("Lowest accepted value")
                .add()
                .append(new KeyedCodec<>("Max", Codec.DOUBLE),
                        (asset, v) -> asset.max = v,
                        asset -> asset.max)
                .documentation("Highest accepted value")
                .add()
                .append(new KeyedCodec<>("Description", Codec.STRING),
                        (asset, v) -> asset.description = v,
                        asset -> asset.description)
                .documentation("Help text shown by /ability available")
                .add()
                .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN),
                        (asset, v) -> asset.enabled = v,
                        asset -> asset.enabled)
                .documentation("Disable to stop the ability being granted without removing its asset")
                .add()
                .build();
    }
}
