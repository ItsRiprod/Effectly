package com.hexvane.abilityapi.builtin.combat;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class ResistanceComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, ResistanceComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, ResistanceComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(ResistanceComponent.class, ResistanceComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new ResistanceComponent();
    }
}
