package com.riprod.effectly.builtin.effects.thorns;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public final class ThornsComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, ThornsComponent> componentType;

    @Nonnull
    public static ComponentType<EntityStore, ThornsComponent> getComponentType() {
        return componentType;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        if (componentType == null) {
            componentType = registry.registerComponent(ThornsComponent.class, ThornsComponent::new);
        }
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new ThornsComponent();
    }
}
