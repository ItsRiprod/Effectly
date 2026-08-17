package com.riprod.abilityapi.builtin.combat;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class StrengthComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, StrengthComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, StrengthComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(StrengthComponent.class, StrengthComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new StrengthComponent();
    }
}
