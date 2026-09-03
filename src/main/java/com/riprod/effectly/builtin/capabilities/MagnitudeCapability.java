package com.riprod.effectly.builtin.capabilities;

import com.riprod.effectly.core.actions.capability.CapabilityType;
import com.riprod.effectly.core.actions.capability.NumericCapability;

public final class MagnitudeCapability implements NumericCapability {

    public static final CapabilityType<MagnitudeCapability> TYPE =
            new CapabilityType<>("Magnitude", MagnitudeCapability.class);

    private final double value;

    public MagnitudeCapability(double value) {
        this.value = value;
    }

    @Override
    public double asNumber() {
        return value;
    }
}
