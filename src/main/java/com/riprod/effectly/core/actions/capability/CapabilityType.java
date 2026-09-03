package com.riprod.effectly.core.actions.capability;

import javax.annotation.Nonnull;

public final class CapabilityType<T extends Capability> {

    public static final int UNREGISTERED = -1;

    private final String id;
    private final Class<T> type;
    private int index = UNREGISTERED;

    public CapabilityType(@Nonnull String id, @Nonnull Class<T> type) {
        this.id = id;
        this.type = type;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public Class<T> getType() {
        return type;
    }

    public boolean isNumeric() {
        return NumericCapability.class.isAssignableFrom(type);
    }

    public int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return id;
    }
}
