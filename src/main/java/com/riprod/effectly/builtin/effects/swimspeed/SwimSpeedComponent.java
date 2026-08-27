package com.riprod.effectly.builtin.effects.swimspeed;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbstractAbilityComponent;

import javax.annotation.Nonnull;

public final class SwimSpeedComponent extends AbstractAbilityComponent {

    private static ComponentType<EntityStore, SwimSpeedComponent> COMPONENT_TYPE;

    private float reassertAccumulator;
    private boolean lastSwimming;

    @Nonnull
    public static ComponentType<EntityStore, SwimSpeedComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(SwimSpeedComponent.class, SwimSpeedComponent::new);
    }

    public boolean due(float dt, float interval, boolean swimming) {
        if (lastSwimming != swimming) {
            lastSwimming = swimming;
            reassertAccumulator = 0f;
            return true;
        }
        reassertAccumulator += dt;
        if (reassertAccumulator < interval) return false;
        reassertAccumulator -= interval;
        return true;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        SwimSpeedComponent copy = new SwimSpeedComponent();
        copy.reassertAccumulator = reassertAccumulator;
        copy.lastSwimming = lastSwimming;
        copy.abilityId = this.abilityId;
        return copy;
    }
}
