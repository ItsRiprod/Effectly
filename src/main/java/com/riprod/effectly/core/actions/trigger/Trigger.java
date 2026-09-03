package com.riprod.effectly.core.actions.trigger;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Trigger {

    private final String id;
    private final Consumer<ComponentRegistryProxy<EntityStore>> installer;

    public Trigger(@Nonnull String id) {
        this(id, null);
    }

    public Trigger(@Nonnull String id, @Nullable Consumer<ComponentRegistryProxy<EntityStore>> installer) {
        this.id = id;
        this.installer = installer;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        if (installer != null) {
            installer.accept(registry);
        }
    }
}
