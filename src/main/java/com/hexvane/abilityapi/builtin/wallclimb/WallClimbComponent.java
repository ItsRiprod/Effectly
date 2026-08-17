package com.hexvane.abilityapi.builtin.wallclimb;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class WallClimbComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, WallClimbComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, WallClimbComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(WallClimbComponent.class, WallClimbComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new WallClimbComponent();
    }
}
