package com.riprod.effectly.builtin.capabilities;

import com.riprod.effectly.core.actions.capability.Capability;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import javax.annotation.Nonnull;

import org.joml.Vector3d;

public final class PositionCapability implements Capability {

    public static final CapabilityType<PositionCapability> TYPE =
            new CapabilityType<>("Position", PositionCapability.class);

    private final Vector3d position;

    public PositionCapability(@Nonnull Vector3d position) {
        this.position = position;
    }

    @Nonnull
    public Vector3d getPosition() {
        return position;
    }
}
