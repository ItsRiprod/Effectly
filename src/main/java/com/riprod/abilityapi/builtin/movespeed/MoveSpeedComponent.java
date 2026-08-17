package com.riprod.abilityapi.builtin.movespeed;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class MoveSpeedComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, MoveSpeedComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, MoveSpeedComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(MoveSpeedComponent.class, MoveSpeedComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new MoveSpeedComponent();
    }
}
