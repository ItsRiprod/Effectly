package com.riprod.effectly.builtin.effects.flight;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbstractAbilityComponent;

import javax.annotation.Nonnull;

public final class FlightComponent extends AbstractAbilityComponent {

    private static ComponentType<EntityStore, FlightComponent> COMPONENT_TYPE;

    private float reassertAccumulator;

    @Nonnull
    public static ComponentType<EntityStore, FlightComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(FlightComponent.class, FlightComponent::new);
    }

    public float addTime(float dt) {
        reassertAccumulator += dt;
        return reassertAccumulator;
    }

    public void consumeTime(float interval) {
        reassertAccumulator -= interval;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        FlightComponent copy = new FlightComponent();
        copy.reassertAccumulator = reassertAccumulator;
        copy.abilityId = this.abilityId;
        return copy;
    }
}
