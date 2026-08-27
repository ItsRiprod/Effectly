package com.riprod.effectly.builtin.effects.survival;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.registry.AbilityContext;
import com.riprod.effectly.core.effects.registry.EffectHandler;

import javax.annotation.Nonnull;

public final class WaterbreathingHandler implements EffectHandler {

    public static final String ID = "waterbreathing";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        WaterbreathingComponent.register(registry);
        registry.registerSystem(new WaterbreathingEventSystem());
        registry.registerSystem(new WaterbreathingRecheckSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        context.getComponents().putComponent(
                context.getRef(), WaterbreathingComponent.getComponentType(), new WaterbreathingComponent());
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().tryRemoveComponent(context.getRef(), WaterbreathingComponent.getComponentType());
    }
}
