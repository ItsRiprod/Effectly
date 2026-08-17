package com.hexvane.abilityapi.builtin.survival;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class WaterbreathingComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, WaterbreathingComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, WaterbreathingComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(WaterbreathingComponent.class, WaterbreathingComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new WaterbreathingComponent();
    }
}
