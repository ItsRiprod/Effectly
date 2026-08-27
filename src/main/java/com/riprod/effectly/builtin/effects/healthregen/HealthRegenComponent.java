package com.riprod.effectly.builtin.effects.healthregen;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbstractAbilityComponent;

import javax.annotation.Nonnull;

public final class HealthRegenComponent extends AbstractAbilityComponent {

    private static ComponentType<EntityStore, HealthRegenComponent> COMPONENT_TYPE;

    private float damageDelayRemaining;

    @Nonnull
    public static ComponentType<EntityStore, HealthRegenComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(HealthRegenComponent.class, HealthRegenComponent::new);
    }

    public void blockFor(float seconds) {
        if (seconds > damageDelayRemaining) damageDelayRemaining = seconds;
    }

    public boolean blocked(float dt) {
        if (damageDelayRemaining <= 0f) return false;
        damageDelayRemaining -= dt;
        return true;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        HealthRegenComponent copy = new HealthRegenComponent();
        copy.damageDelayRemaining = damageDelayRemaining;
        copy.abilityId = this.abilityId;
        return copy;
    }
}
