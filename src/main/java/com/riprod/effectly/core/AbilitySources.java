package com.riprod.effectly.core;

import javax.annotation.Nonnull;

public final class AbilitySources {

    public static final String API = "api";
    public static final String EQUIPMENT = "equipment";

    private static final String EQUIPMENT_PREFIX = EQUIPMENT + ":";

    private AbilitySources() {}

    @Nonnull
    public static String equipment(@Nonnull String slot) {
        return EQUIPMENT_PREFIX + slot;
    }

    public static boolean isEquipment(@Nonnull String sourceId) {
        return sourceId.equals(EQUIPMENT) || sourceId.startsWith(EQUIPMENT_PREFIX);
    }
}
