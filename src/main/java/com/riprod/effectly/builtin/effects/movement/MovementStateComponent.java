package com.riprod.effectly.builtin.effects.movement;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Marker that makes an entity visible to {@link MovementStateSystem}. Carries no per-ability state:
 * the system reads the roster's resolved view, so one marker serves any number of movement abilities.
 */
public final class MovementStateComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, MovementStateComponent> componentType;

    @Nonnull
    public static ComponentType<EntityStore, MovementStateComponent> getComponentType() {
        return componentType;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        if (componentType == null) {
            componentType = registry.registerComponent(
                    MovementStateComponent.class, MovementStateComponent::new);
        }
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new MovementStateComponent();
    }
}
