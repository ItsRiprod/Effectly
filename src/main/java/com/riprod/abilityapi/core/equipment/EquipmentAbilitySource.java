package com.riprod.abilityapi.core.equipment;

import com.riprod.abilityapi.core.asset.AbilityAsset;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EquipmentAbilitySource {

    private EquipmentAbilitySource() {}

    public static void collect(@Nullable ItemStack stack, @Nonnull Map<String, Double> out) {
        if (ItemStack.isEmpty(stack)) return;

        Item item = stack.getItem();
        if (item != null) {
            String[] tagged = EquipmentAbilityTag.read(item);
            if (tagged != null) {
                for (String value : tagged) {
                    fold(out, EquipmentAbilityTag.abilityIdOf(value), EquipmentAbilityTag.valueOf(value));
                }
            }
        }

        Map<String, Double> metadata = EquipmentAbilityMetadata.read(stack);
        if (metadata != null) {
            for (Map.Entry<String, Double> granted : metadata.entrySet()) {
                fold(out, granted.getKey(), granted.getValue());
            }
        }
    }

    private static void fold(@Nonnull Map<String, Double> out, @Nonnull String abilityId, @Nullable Double requested) {
        AbilityAsset asset = AbilityAsset.getAssetMap().getAsset(abilityId);
        if (asset == null || !asset.isEnabled()) return;

        double value = requested == null ? asset.getDefaultValue() : requested;
        value = Math.max(asset.getMin(), Math.min(asset.getMax(), value));

        Double existing = out.get(abilityId);
        if (existing == null || value > existing) out.put(abilityId, value);
    }
}
