package com.riprod.effectly.core.equipment;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EquipmentAbilityTag {

    public static final String TAG = "Effectly:Ability";
    public static final String CONDITION_TAG = "Effectly:Condition";

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final char VALUE_SEPARATOR = ':';

    private static final int TAG_INDEX = AssetRegistry.getOrCreateTagIndex(TAG);
    private static final int CONDITION_TAG_INDEX = AssetRegistry.getOrCreateTagIndex(CONDITION_TAG);

    private EquipmentAbilityTag() {}

    @Nullable
    public static String[] read(@Nonnull Item item) {
        return readTag(item, TAG_INDEX, TAG);
    }

    @Nullable
    public static String[] readConditions(@Nonnull Item item) {
        return readTag(item, CONDITION_TAG_INDEX, CONDITION_TAG);
    }

    @Nullable
    private static String[] readTag(@Nonnull Item item, int tagIndex, @Nonnull String tag) {
        AssetExtraInfo.Data data = item.getData();
        if (data == null) return null;
        if (!data.getExpandedTagIndexes().contains(tagIndex)) return null;

        Map<String, String[]> rawTags = data.getRawTags();
        String[] values = rawTags.get(tag);
        return values == null || values.length == 0 ? null : values;
    }

    @Nonnull
    public static String abilityIdOf(@Nonnull String value) {
        int separator = value.indexOf(VALUE_SEPARATOR);
        return separator < 0 ? value : value.substring(0, separator);
    }

    @Nullable
    public static Double valueOf(@Nonnull String value) {
        int separator = value.indexOf(VALUE_SEPARATOR);
        if (separator < 0) return null;

        int end = value.indexOf(VALUE_SEPARATOR, separator + 1);
        String raw = (end < 0 ? value.substring(separator + 1) : value.substring(separator + 1, end)).trim();
        if (raw.isEmpty()) return null;
        try {
            return Double.valueOf(raw);
        } catch (NumberFormatException e) {
            LOGGER.atWarning().log("Tag '%s' value '%s' is not a number; using the ability default", TAG, value);
            return null;
        }
    }

    @Nullable
    public static String conditionsOf(@Nonnull String value) {
        int separator = value.indexOf(VALUE_SEPARATOR);
        if (separator < 0) return null;
        int end = value.indexOf(VALUE_SEPARATOR, separator + 1);
        if (end < 0) return null;
        String raw = value.substring(end + 1).trim();
        return raw.isEmpty() ? null : raw;
    }
}
