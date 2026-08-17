package com.hexvane.abilityapi.builtin.oxygen;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class OxygenComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, OxygenComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, OxygenComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(OxygenComponent.class, OxygenComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new OxygenComponent();
    }
}
