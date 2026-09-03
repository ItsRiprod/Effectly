package com.riprod.effectly.builtin.effects.thorns;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.registry.AbilityContext;
import com.riprod.effectly.core.abilities.registry.AbilityHandlerRegistry;
import com.riprod.effectly.core.effects.registry.AbilityHandler;

import javax.annotation.Nonnull;

public final class ThornsHandler implements AbilityHandler {

    public static final String ID = "Thorns";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<ThornsConfig> getConfigBinding() {
        return ConfigBinding.of(ThornsConfig.class, ThornsConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        ThornsComponent.register(registry);
        registry.registerSystem(new ThornsSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        if (context.getComponents().getComponent(
                context.getRef(), ThornsComponent.getComponentType()) != null) {
            return;
        }
        context.getComponents().putComponent(
                context.getRef(), ThornsComponent.getComponentType(), new ThornsComponent());
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        if (AbilityHandlerRegistry.holdsAnyFor(context, this)) return;
        context.getComponents().tryRemoveComponent(context.getRef(), ThornsComponent.getComponentType());
    }
}
