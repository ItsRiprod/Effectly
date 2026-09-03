package com.riprod.effectly.builtin.capabilities;

import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.riprod.effectly.core.actions.capability.Capability;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import javax.annotation.Nonnull;

public final class BlockDamageCapability implements Capability {

    public static final CapabilityType<BlockDamageCapability> TYPE =
            new CapabilityType<>("Block_Damage", BlockDamageCapability.class);

    private final DamageBlockEvent event;

    public BlockDamageCapability(@Nonnull DamageBlockEvent event) {
        this.event = event;
    }

    public float getDamage() {
        return event.getDamage();
    }

    public void setDamage(float damage) {
        event.setDamage(damage);
    }
}
