package com.riprod.effectly.core.effects.registry;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.AssetMapWithIndexes;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;
import com.riprod.effectly.core.effects.utils.AbilityType;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public final class EffectAsset implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, EffectAsset>> {

    public static final String ASSET_PATH = "Effectly/Effects";

    public static final int NOT_FOUND = AssetMapWithIndexes.NOT_FOUND;

    public static final AssetBuilderCodec<String, EffectAsset> CODEC = buildCodec();

    private static AssetStore<String, EffectAsset, IndexedLookupTableAssetMap<String, EffectAsset>> ASSET_STORE;

    private AssetExtraInfo.Data data;
    private String id;
    private AbilityHandlerConfig handler;
    private String description;
    private AbilityType type = AbilityType.NUMERIC;
    private double defaultValue = 1.0;
    private double min = 0.0;
    private double max = 1.0;
    private boolean enabled = true;
    private List<AbilityConditionSpec> conditions = List.of();
    private List<Action> actions = List.of();

    private EffectAsset() {
    }

    public static AssetStore<String, EffectAsset, IndexedLookupTableAssetMap<String, EffectAsset>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(EffectAsset.class);
        }
        return ASSET_STORE;
    }

    @SuppressWarnings("unchecked")
    public static IndexedLookupTableAssetMap<String, EffectAsset> getAssetMap() {
        return (IndexedLookupTableAssetMap<String, EffectAsset>) getAssetStore().getAssetMap();
    }

    /** Asset map, or null before the store is registered, so a early grant warns instead of throwing. */
    @Nullable
    private static IndexedLookupTableAssetMap<String, EffectAsset> assetMapOrNull() {
        try {
            return getAssetMap();
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Nullable
    public static EffectAsset get(@Nonnull String abilityId) {
        IndexedLookupTableAssetMap<String, EffectAsset> map = assetMapOrNull();
        return map != null ? map.getAsset(abilityId) : null;
    }

    /**
     * Index for an ability id, resolved once and then used for array-speed lookups. Never persist
     * one: indexes are assigned in load order and are not stable across restarts.
     */
    public static int indexOf(@Nonnull String abilityId) {
        IndexedLookupTableAssetMap<String, EffectAsset> map = assetMapOrNull();
        return map != null ? map.getIndex(abilityId) : NOT_FOUND;
    }

    @Nullable
    public static EffectAsset byIndex(int index) {
        if (index == NOT_FOUND) return null;
        IndexedLookupTableAssetMap<String, EffectAsset> map = assetMapOrNull();
        return map != null ? map.getAsset(index) : null;
    }

    /**
     * Placeholder for an asset removed while the server is running. Indexes are array slots, so a
     * removal cannot leave a hole; a disabled stub keeps the slot and makes grants no-op.
     */
    @Nonnull
    public static EffectAsset getDisabledFor(@Nonnull String key) {
        EffectAsset stub = new EffectAsset();
        stub.id = key;
        stub.enabled = false;
        return stub;
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

    /**
     * Conditions intrinsic to the effect itself, gating it regardless of who granted it. AND-ed with
     * whatever conditions the individual grant carries.
     */
    @Nonnull
    public List<AbilityConditionSpec> getConditions() {
        return this.conditions;
    }

    @Nonnull
    public static List<AbilityConditionSpec> conditionsFor(@Nonnull String abilityId) {
        EffectAsset asset = get(abilityId);
        return asset == null ? List.of() : asset.conditions;
    }

    @Nonnull
    public List<Action> getActions() {
        return this.actions;
    }

    public boolean hasActions() {
        return !this.actions.isEmpty();
    }

    @Nullable
    public AbilityHandlerConfig getHandlerConfig() {
        return this.handler;
    }

    public static boolean isEnabled(@Nonnull String abilityId) {
        EffectAsset asset = get(abilityId);
        return asset != null && asset.enabled;
    }

    public static double clampToRange(@Nonnull String abilityId, double value) {
        EffectAsset asset = get(abilityId);
        if (asset == null) return value;
        if (value < asset.min) return asset.min;
        if (value > asset.max) return asset.max;
        return value;
    }

    @Nullable
    public static <T extends AbilityHandlerConfig> T configFor(
            @Nonnull String abilityId, @Nonnull Class<T> type) {
        EffectAsset asset = get(abilityId);
        AbilityHandlerConfig config = asset != null ? asset.handler : null;
        return type.isInstance(config) ? type.cast(config) : null;
    }

    private static AssetBuilderCodec<String, @NotNull EffectAsset> buildCodec() {
        return AssetBuilderCodec
                .builder(EffectAsset.class, EffectAsset::new, Codec.STRING,
                        (asset, key) -> asset.id = key,
                        asset -> asset.id,
                        (asset, data) -> asset.data = data,
                        asset -> asset.data)
                .append(new KeyedCodec<>("Handler", AbilityHandlerConfig.CODEC),
                        (asset, v) -> asset.handler = v,
                        asset -> asset.handler)
                .documentation("The handler that implements this ability, plus its tuning. Id names a "
                        + "registered handler and selects the shape of the rest of the block; several "
                        + "ability assets may share one handler. Defaults to the asset id.")
                .metadata(new UIEditorSectionStart("Handler"))
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
                .append(new KeyedCodec<>("Conditions",
                                new ArrayCodec<>(AbilityConditionSpec.CODEC, AbilityConditionSpec[]::new)),
                        (asset, v) -> asset.conditions = v == null ? List.of() : List.of(v),
                        asset -> asset.conditions.isEmpty()
                                ? null
                                : asset.conditions.toArray(AbilityConditionSpec[]::new))
                .documentation("Conditions intrinsic to this effect, gating it for every source that "
                        + "grants it. AND-ed with the grant's own conditions")
                .add()
                .append(new KeyedCodec<>("Actions",
                                new ArrayCodec<>(Action.CODEC, Action[]::new)),
                        (asset, v) -> asset.actions = v == null ? List.of() : List.of(v),
                        asset -> asset.actions.isEmpty()
                                ? null
                                : asset.actions.toArray(Action[]::new))
                .documentation("One-shot actions executed when their trigger fires, scaled by the "
                        + "granted value. An effect may have actions, a handler, or both")
                .add()
                .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN),
                        (asset, v) -> asset.enabled = v,
                        asset -> asset.enabled)
                .documentation("Disable to stop the ability being granted without removing its asset")
                .add()
                .build();
    }
}
