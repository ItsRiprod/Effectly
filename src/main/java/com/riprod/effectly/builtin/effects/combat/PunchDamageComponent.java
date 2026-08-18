package com.riprod.effectly.builtin.effects.combat;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class PunchDamageComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, PunchDamageComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, PunchDamageComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(PunchDamageComponent.class, PunchDamageComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new PunchDamageComponent();
    }
}
