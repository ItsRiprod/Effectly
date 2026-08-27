package com.riprod.effectly.builtin.effects.mining;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbstractAbilityComponent;

import javax.annotation.Nonnull;

public final class MiningHasteComponent extends AbstractAbilityComponent {

    private static ComponentType<EntityStore, MiningHasteComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, MiningHasteComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(MiningHasteComponent.class, MiningHasteComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        MiningHasteComponent copy = new MiningHasteComponent();
        copy.abilityId = this.abilityId;
        return copy;
    }
}
