package com.riprod.effectly.builtin.effects.darkvision;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbstractAbilityComponent;

import javax.annotation.Nonnull;

public final class DarkVisionComponent extends AbstractAbilityComponent {

    private static ComponentType<EntityStore, DarkVisionComponent> COMPONENT_TYPE;

    private float accumulator;
    private boolean applyNow = true;

    @Nonnull
    public static ComponentType<EntityStore, DarkVisionComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(DarkVisionComponent.class, DarkVisionComponent::new);
    }

    public void requestApply() {
        applyNow = true;
    }

    public boolean due(float dt, float interval) {
        if (applyNow) {
            applyNow = false;
            accumulator = 0f;
            return true;
        }
        accumulator += dt;
        if (accumulator < interval) return false;
        accumulator -= interval;
        return true;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        DarkVisionComponent copy = new DarkVisionComponent();
        copy.accumulator = accumulator;
        copy.applyNow = applyNow;
        copy.abilityId = this.abilityId;
        return copy;
    }
}
