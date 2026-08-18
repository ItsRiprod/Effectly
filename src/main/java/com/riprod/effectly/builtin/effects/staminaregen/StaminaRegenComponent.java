package com.riprod.effectly.builtin.effects.staminaregen;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class StaminaRegenComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, StaminaRegenComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, StaminaRegenComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(StaminaRegenComponent.class, StaminaRegenComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new StaminaRegenComponent();
    }
}
