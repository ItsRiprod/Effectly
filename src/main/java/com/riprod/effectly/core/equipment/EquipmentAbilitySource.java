package com.riprod.effectly.core.equipment;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;
import com.riprod.effectly.core.conditions.registry.ConditionRegistery;
import com.riprod.effectly.core.effects.registry.EffectAsset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EquipmentAbilitySource {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Pattern SPACES = Pattern.compile("\\s+");

    private EquipmentAbilitySource() {}

    public static void collect(@Nullable ItemStack stack, @Nonnull Map<String, EquipmentGrant> out) {
        if (ItemStack.isEmpty(stack)) return;

        Item item = stack.getItem();
        String itemId = stack.getItemId();
        List<AbilityConditionSpec> itemConditions = itemConditions(item, stack, itemId);

        if (item != null) {
            String[] tagged = EquipmentAbilityTag.read(item);
            if (tagged != null) {
                for (String value : tagged) {
                    String perAbility = EquipmentAbilityTag.conditionsOf(value);
                    fold(out, EquipmentAbilityTag.abilityIdOf(value), EquipmentAbilityTag.valueOf(value),
                            perAbility == null ? itemConditions : parse(perAbility, itemId));
                }
            }
        }

        Map<String, Double> metadata = EquipmentAbilityMetadata.read(stack);
        if (metadata != null) {
            for (Map.Entry<String, Double> granted : metadata.entrySet()) {
                fold(out, granted.getKey(), granted.getValue(), itemConditions);
            }
        }
    }

    @Nonnull
    private static List<AbilityConditionSpec> itemConditions(
            @Nullable Item item, @Nonnull ItemStack stack, @Nullable String itemId) {
        List<AbilityConditionSpec> out = new ArrayList<>();
        if (item != null) {
            String[] tagged = EquipmentAbilityTag.readConditions(item);
            if (tagged != null) {
                for (String value : tagged) out.addAll(parse(value, itemId));
            }
        }
        String[] metadata = EquipmentAbilityMetadata.readConditions(stack);
        if (metadata != null) {
            for (String value : metadata) out.addAll(parse(value, itemId));
        }
        return out;
    }

    @Nonnull
    private static List<AbilityConditionSpec> parse(@Nonnull String raw, @Nullable String itemId) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return List.of();

        List<String> unparsed = new ArrayList<>();
        List<AbilityConditionSpec> specs = ConditionRegistery.parse(SPACES.split(trimmed), unparsed);
        if (!unparsed.isEmpty()) {
            LOGGER.atWarning().atMostEvery(1, TimeUnit.MINUTES)
                    .log("Item '%s' condition '%s' has unrecognised token(s) %s; "
                            + "the ability is granted without them", itemId, trimmed, unparsed);
        }
        return specs;
    }

    private static void fold(
            @Nonnull Map<String, EquipmentGrant> out,
            @Nonnull String abilityId,
            @Nullable Double requested,
            @Nonnull List<AbilityConditionSpec> conditions) {
        EffectAsset asset = EffectAsset.getAssetMap().getAsset(abilityId);
        if (asset == null || !asset.isEnabled()) return;

        double value = requested == null ? asset.getDefaultValue() : requested;
        value = Math.max(asset.getMin(), Math.min(asset.getMax(), value));

        EquipmentGrant existing = out.get(abilityId);
        if (existing == null || value > existing.value()) {
            out.put(abilityId, new EquipmentGrant(value, conditions));
        }
    }
}
