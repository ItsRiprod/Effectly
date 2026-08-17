package com.riprod.abilityapi.builtin.mining;

import com.riprod.abilityapi.core.AbilityComponent;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class MiningFortuneComponent extends AbilityComponent {

    private static ComponentType<EntityStore, MiningFortuneComponent> COMPONENT_TYPE;

    @Nonnull
    public static ComponentType<EntityStore, MiningFortuneComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(MiningFortuneComponent.class, MiningFortuneComponent::new);
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        MiningFortuneComponent copy = new MiningFortuneComponent();
        copy.abilityId = this.abilityId;
        return copy;
    }
}
