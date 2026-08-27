package com.riprod.effectly.builtin.effects.itemmagnet;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbstractAbilityComponent;

import javax.annotation.Nonnull;

public final class ItemMagnetComponent extends AbstractAbilityComponent {

    private static ComponentType<EntityStore, ItemMagnetComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, ItemMagnetComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(ItemMagnetComponent.class, ItemMagnetComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        ItemMagnetComponent copy = new ItemMagnetComponent();
        copy.abilityId = this.abilityId;
        return copy;
    }
}
