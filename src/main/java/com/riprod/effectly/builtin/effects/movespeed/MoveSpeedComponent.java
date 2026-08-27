package com.riprod.effectly.builtin.effects.movespeed;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbstractAbilityComponent;

import javax.annotation.Nonnull;

public final class MoveSpeedComponent extends AbstractAbilityComponent {

    private static ComponentType<EntityStore, MoveSpeedComponent> COMPONENT_TYPE;

    private float reassertAccumulator;

    @Nonnull
    public static ComponentType<EntityStore, MoveSpeedComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(MoveSpeedComponent.class, MoveSpeedComponent::new);
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
        MoveSpeedComponent copy = new MoveSpeedComponent();
        copy.reassertAccumulator = reassertAccumulator;
        copy.abilityId = this.abilityId;
        return copy;
    }
}
