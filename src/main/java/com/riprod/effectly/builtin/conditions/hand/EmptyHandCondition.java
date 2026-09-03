package com.riprod.effectly.builtin.conditions.hand;

import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;
import com.riprod.effectly.core.conditions.ConditionContext;
import com.riprod.effectly.core.conditions.registry.ConditionAsset;
import com.riprod.effectly.core.conditions.registry.ConditionHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EmptyHandCondition implements ConditionHandler {

    public static final String ID = "empty_hand";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean test(
            @Nonnull ConditionContext context,
            @Nonnull ConditionAsset asset,
            @Nonnull AbilityConditionSpec spec) {
        return ItemStack.isEmpty(
                InventoryComponent.getItemInHand(context.getComponents(), context.getRef()));
    }

    @Nullable
    @Override
    public Parsed parse(@Nonnull ConditionAsset asset, @Nonnull String[] remaining) {
        return new Parsed(new AbilityConditionSpec(asset.getId()), 0);
    }
}
