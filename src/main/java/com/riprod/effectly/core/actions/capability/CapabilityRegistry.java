package com.riprod.effectly.core.actions.capability;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CapabilityRegistry {

    private static final Map<String, CapabilityType<?>> CAPABILITIES = new Object2ObjectLinkedOpenHashMap<>();

    private CapabilityRegistry() {}

    public static void reset() {
        for (CapabilityType<?> capability : CAPABILITIES.values()) {
            capability.setIndex(CapabilityType.UNREGISTERED);
        }
        CAPABILITIES.clear();
    }

    @Nonnull
    public static <T extends Capability> CapabilityType<T> register(@Nonnull CapabilityType<T> capability) {
        CapabilityType<?> existing = CAPABILITIES.putIfAbsent(capability.getId(), capability);
        if (existing != null && existing != capability) {
            throw new IllegalArgumentException("duplicate capability id: " + capability.getId());
        }
        if (existing == null) {
            capability.setIndex(CAPABILITIES.size() - 1);
        }
        return capability;
    }

    @Nullable
    public static CapabilityType<?> byId(@Nonnull String id) {
        return CAPABILITIES.get(id);
    }

    public static int count() {
        return CAPABILITIES.size();
    }

    @Nonnull
    public static Collection<String> ids() {
        return Collections.unmodifiableCollection(CAPABILITIES.keySet());
    }
}
