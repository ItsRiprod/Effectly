package com.riprod.effectly.builtin.effects.oxygen;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.AbilityComponent;

import javax.annotation.Nonnull;

public final class OxygenComponent extends AbilityComponent {

    private static ComponentType<EntityStore, OxygenComponent> COMPONENT_TYPE;

    private float accumulator;
    private boolean recheckNow = true;
    private float appliedAmount;

    @Nonnull
    public static ComponentType<EntityStore, OxygenComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(OxygenComponent.class, OxygenComponent::new);
    }

    public void requestRecheck() {
        this.recheckNow = true;
    }

    public boolean due(float dt, float interval) {
        if (recheckNow) {
            recheckNow = false;
            accumulator = 0f;
            return true;
        }
        accumulator += dt;
        if (accumulator < interval) return false;
        accumulator -= interval;
        return true;
    }

    public float getAppliedAmount() {
        return this.appliedAmount;
    }

    public void setAppliedAmount(float appliedAmount) {
        this.appliedAmount = appliedAmount;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        OxygenComponent copy = new OxygenComponent();
        copy.accumulator = accumulator;
        copy.recheckNow = recheckNow;
        copy.appliedAmount = appliedAmount;
        copy.abilityId = this.abilityId;
        return copy;
    }
}
