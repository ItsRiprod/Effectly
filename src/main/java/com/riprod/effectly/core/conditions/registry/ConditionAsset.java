package com.riprod.effectly.core.conditions.registry;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ConditionAsset
        implements JsonAssetWithMap<String, DefaultAssetMap<String, ConditionAsset>> {

    public static final String ASSET_PATH = "Effectly/Conditions";

    public static final AssetBuilderCodec<String, ConditionAsset> CODEC = buildCodec();

    private static AssetStore<String, ConditionAsset, DefaultAssetMap<String, ConditionAsset>> ASSET_STORE;

    private AssetExtraInfo.Data data;
    private String id;
    private ConditionConfig handler;
    private String keyword;
    private String description;
    private boolean enabled = true;

    private ConditionAsset() {
    }

    public static AssetStore<String, ConditionAsset, DefaultAssetMap<String, ConditionAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(ConditionAsset.class);
        }
        return ASSET_STORE;
    }

    @SuppressWarnings("unchecked")
    public static DefaultAssetMap<String, ConditionAsset> getAssetMap() {
        return (DefaultAssetMap<String, ConditionAsset>) getAssetStore().getAssetMap();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Nonnull
    public String getHandler() {
        String handlerId = this.handler != null ? this.handler.getId() : null;
        return handlerId == null ? this.id : handlerId;
    }

    @Nonnull
    public String getKeyword() {
        return this.keyword == null ? this.id : this.keyword;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    @Nullable
    public ConditionConfig getHandlerConfig() {
        return this.handler;
    }

    @Nullable
    public static ConditionAsset get(@Nonnull String conditionId) {
        return getAssetMap().getAsset(conditionId);
    }

    public static boolean isEnabled(@Nonnull String conditionId) {
        ConditionAsset asset = getAssetMap().getAsset(conditionId);
        return asset != null && asset.enabled;
    }

    @Nullable
    public static <T extends ConditionConfig> T configFor(
            @Nonnull String conditionId, @Nonnull Class<T> type) {
        ConditionAsset asset = getAssetMap().getAsset(conditionId);
        ConditionConfig config = asset != null ? asset.handler : null;
        return type.isInstance(config) ? type.cast(config) : null;
    }

    @Nullable
    public <T extends ConditionConfig> T config(@Nonnull Class<T> type) {
        return type.isInstance(this.handler) ? type.cast(this.handler) : null;
    }

    @Nonnull
    public <T extends ConditionConfig> T configOrDefault(@Nonnull Class<T> type, @Nonnull T fallback) {
        T config = config(type);
        return config != null ? config : fallback;
    }

    private static AssetBuilderCodec<String, ConditionAsset> buildCodec() {
        return AssetBuilderCodec
                .builder(ConditionAsset.class, ConditionAsset::new, Codec.STRING,
                        (asset, key) -> asset.id = key,
                        asset -> asset.id,
                        (asset, data) -> asset.data = data,
                        asset -> asset.data)
                .append(new KeyedCodec<>("Handler", ConditionConfig.CODEC),
                        (asset, v) -> asset.handler = v,
                        asset -> asset.handler)
                .documentation("The handler that implements this condition, plus its tuning. Id names a "
                        + "registered condition handler and selects the shape of the rest of the block; "
                        + "several condition assets may share one handler. Defaults to the asset id.")
                .metadata(new UIEditorSectionStart("Handler"))
                .add()
                .append(new KeyedCodec<>("Keyword", Codec.STRING),
                        (asset, v) -> asset.keyword = v,
                        asset -> asset.keyword)
                .documentation("Token that selects this condition on the /ability add command line. "
                        + "Defaults to the asset id")
                .add()
                .append(new KeyedCodec<>("Description", Codec.STRING),
                        (asset, v) -> asset.description = v,
                        asset -> asset.description)
                .documentation("Help text shown in the condition list by /ability add --help")
                .add()
                .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN),
                        (asset, v) -> asset.enabled = v,
                        asset -> asset.enabled)
                .documentation("Disable to stop the condition being evaluated without removing its asset. "
                        + "A grant referencing a disabled condition never activates")
                .add()
                .build();
    }
}
