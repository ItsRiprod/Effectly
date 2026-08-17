package com.riprod.abilityapi.core.equipment;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EquipmentAbilityTag {

    public static final String TAG = "AbilityAPI:Ability";

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final char VALUE_SEPARATOR = ':';

    private static final int TAG_INDEX = AssetRegistry.getOrCreateTagIndex(TAG);

    private EquipmentAbilityTag() {}

    @Nullable
    public static String[] read(@Nonnull Item item) {
        AssetExtraInfo.Data data = item.getData();
        if (data == null) return null;
        if (!data.getExpandedTagIndexes().contains(TAG_INDEX)) return null;

        Map<String, String[]> rawTags = data.getRawTags();
        String[] values = rawTags.get(TAG);
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
        String raw = value.substring(separator + 1);
        try {
            return Double.valueOf(raw);
        } catch (NumberFormatException e) {
            LOGGER.atWarning().log("Tag '%s' value '%s' is not a number; using the ability default", TAG, value);
            return null;
        }
    }
}
