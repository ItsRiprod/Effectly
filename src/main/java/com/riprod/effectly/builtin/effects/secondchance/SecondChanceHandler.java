package com.riprod.effectly.builtin.effects.secondchance;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.registry.AbilityContext;
import com.riprod.effectly.core.effects.registry.AbilityHandler;

import javax.annotation.Nonnull;

public final class SecondChanceHandler implements AbilityHandler {

    public static final String ID = "second_chance";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<SecondChanceConfig> getConfigBinding() {
        return ConfigBinding.of(SecondChanceConfig.class, SecondChanceConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        SecondChanceComponent.register(registry);
        registry.registerSystem(new SecondChanceSystem());
        registry.registerSystem(new SecondChanceCooldownSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        SecondChanceComponent component = context.getComponents().getComponent(context.getRef(), SecondChanceComponent.getComponentType());
        if (component == null) component = new SecondChanceComponent();
        component.bind(abilityId);
        context.getComponents().putComponent(context.getRef(), SecondChanceComponent.getComponentType(), component);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().tryRemoveComponent(context.getRef(), SecondChanceComponent.getComponentType());
    }
}
