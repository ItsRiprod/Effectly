package com.riprod.effectly.core.equipment;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EquipmentAbilityMetadata {

    public static final String KEY = "Effectly:Abilities";
    public static final String CONDITIONS_KEY = "Effectly:Conditions";

    @Nonnull
    public static final Codec<Map<String, Double>> CODEC =
            new MapCodec<>(Codec.DOUBLE, LinkedHashMap::new, false);

    private EquipmentAbilityMetadata() {}

    @Nullable
    public static Map<String, Double> read(@Nonnull ItemStack stack) {
        Map<String, Double> abilities = stack.getFromMetadataOrNull(KEY, CODEC);
        return abilities == null || abilities.isEmpty() ? null : abilities;
    }

    @Nullable
    public static String[] readConditions(@Nonnull ItemStack stack) {
        String[] conditions = stack.getFromMetadataOrNull(CONDITIONS_KEY, Codec.STRING_ARRAY);
        return conditions == null || conditions.length == 0 ? null : conditions;
    }

    @Nonnull
    public static ItemStack write(@Nonnull ItemStack stack, @Nullable Map<String, Double> abilities) {
        return stack.withMetadata(KEY, CODEC, abilities == null || abilities.isEmpty() ? null : abilities);
    }
}
