package com.riprod.effectly.core.actions.trigger;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Trigger {

    private final String id;
    private final Set<CapabilityType<?>> provides;
    private final Consumer<ComponentRegistryProxy<EntityStore>> installer;

    public Trigger(
            @Nonnull String id,
            @Nonnull Set<CapabilityType<?>> provides,
            @Nullable Consumer<ComponentRegistryProxy<EntityStore>> installer) {
        this.id = id;
        this.provides = provides;
        this.installer = installer;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public Set<CapabilityType<?>> getProvides() {
        return provides;
    }

    public boolean provides(@Nonnull CapabilityType<?> capability) {
        return provides.contains(capability);
    }

    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        if (installer != null) {
            installer.accept(registry);
        }
    }
}
