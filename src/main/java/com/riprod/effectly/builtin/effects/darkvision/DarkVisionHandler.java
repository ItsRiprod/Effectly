package com.riprod.effectly.builtin.effects.darkvision;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.AbilityContext;
import com.riprod.effectly.core.AbilityEntry;
import com.riprod.effectly.core.AbilityHandler;

import javax.annotation.Nonnull;

public final class DarkVisionHandler implements AbilityHandler {

    public static final String ID = "dark_vision";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<DarkVisionConfig> getConfigBinding() {
        return ConfigBinding.of(DarkVisionConfig.class, DarkVisionConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        DarkVisionComponent.register(registry);
        registry.registerSystem(new DarkVisionEffectSystem());
        registry.registerSystem(new DarkVisionCleanupSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        DarkVisionComponent state = context.getComponents().getComponent(context.getRef(), DarkVisionComponent.getComponentType());
        if (state == null) state = new DarkVisionComponent();
        state.bind(abilityId);
        state.requestApply();
        context.getComponents().putComponent(context.getRef(), DarkVisionComponent.getComponentType(), state);
    }

    @Override
    public void reconcile(@Nonnull AbilityContext context) {
        DarkVisionComponent state = context.getComponents().getComponent(context.getRef(), DarkVisionComponent.getComponentType());
        DarkVisionConfig config = state != null
                ? state.configOrDefault(DarkVisionConfig.class, DarkVisionConfig.DEFAULTS)
                : DarkVisionConfig.DEFAULTS;
        DarkVisionEffects.remove(context.getRef(), context.getComponents(), config);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().tryRemoveComponent(context.getRef(), DarkVisionComponent.getComponentType());
    }
}
