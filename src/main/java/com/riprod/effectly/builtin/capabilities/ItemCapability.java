package com.riprod.effectly.builtin.capabilities;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.riprod.effectly.core.actions.capability.Capability;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import javax.annotation.Nonnull;

public final class ItemCapability implements Capability {

    public static final CapabilityType<ItemCapability> TYPE =
            new CapabilityType<>("Item", ItemCapability.class);

    private final ItemStack itemStack;

    public ItemCapability(@Nonnull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Nonnull
    public ItemStack getItemStack() {
        return itemStack;
    }
}
