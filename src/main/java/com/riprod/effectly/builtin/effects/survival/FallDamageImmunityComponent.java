package com.riprod.effectly.builtin.effects.survival;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class FallDamageImmunityComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, FallDamageImmunityComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, FallDamageImmunityComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(FallDamageImmunityComponent.class, FallDamageImmunityComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new FallDamageImmunityComponent();
    }
}
