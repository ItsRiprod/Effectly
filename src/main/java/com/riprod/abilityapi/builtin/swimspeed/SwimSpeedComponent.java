package com.riprod.abilityapi.builtin.swimspeed;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class SwimSpeedComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, SwimSpeedComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, SwimSpeedComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(SwimSpeedComponent.class, SwimSpeedComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new SwimSpeedComponent();
    }
}
