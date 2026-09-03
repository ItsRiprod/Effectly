package com.riprod.effectly.core.actions.trigger;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TriggerRegistry {

    private static final Map<String, Trigger> TRIGGERS = new LinkedHashMap<>();

    private TriggerRegistry() {}

    public static void reset() {
        TRIGGERS.clear();
    }

    public static void register(@Nonnull Trigger trigger) {
        Trigger existing = TRIGGERS.putIfAbsent(trigger.getId(), trigger);
        if (existing != null) {
            throw new IllegalArgumentException("duplicate trigger id: " + trigger.getId());
        }
    }

    public static void installAll(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        for (Trigger trigger : TRIGGERS.values()) {
            trigger.install(registry);
        }
    }

    @Nullable
    public static Trigger get(@Nonnull String id) {
        return TRIGGERS.get(id);
    }

    @Nonnull
    public static Collection<String> ids() {
        return Collections.unmodifiableCollection(TRIGGERS.keySet());
    }
}
