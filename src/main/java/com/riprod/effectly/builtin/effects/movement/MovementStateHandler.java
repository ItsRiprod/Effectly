package com.riprod.effectly.builtin.effects.movement;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.registry.AbilityContext;
import com.riprod.effectly.core.effects.registry.EffectHandler;

import javax.annotation.Nonnull;

public final class MovementStateHandler implements EffectHandler {

    public static final String ID = "movement_state";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<MovementStateConfig> getConfigBinding() {
        return ConfigBinding.of(MovementStateConfig.class, MovementStateConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        MovementStateComponent.register(registry);
        registry.registerSystem(new MovementStateSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        if (context.getComponents().getComponent(
                context.getRef(), MovementStateComponent.getComponentType()) != null) {
            return;
        }
        context.getComponents().putComponent(
                context.getRef(), MovementStateComponent.getComponentType(), new MovementStateComponent());
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        // Deliberately keeps the marker: the system still has to fold to defaults and push the
        // revert, and it removes itself once the settings are back to baseline.
    }
}
