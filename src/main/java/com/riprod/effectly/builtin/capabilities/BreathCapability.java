package com.riprod.effectly.builtin.capabilities;

import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.event.events.ecs.BreathingCheckEvent;
import com.riprod.effectly.core.actions.capability.Capability;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import javax.annotation.Nonnull;

public final class BreathCapability implements Capability {

    public static final CapabilityType<BreathCapability> TYPE =
            new CapabilityType<>("Breath", BreathCapability.class);

    private final BreathingCheckEvent event;

    public BreathCapability(@Nonnull BreathingCheckEvent event) {
        this.event = event;
    }

    @Nonnull
    public BlockMaterial getMaterial() {
        return event.getBreathingMaterial();
    }

    public int getFluidId() {
        return event.getFluidId();
    }

    public boolean canBreathe() {
        return event.canBreathe();
    }

    public void setCanBreathe(boolean canBreathe) {
        event.setCanBreathe(canBreathe);
    }
}
