package com.riprod.effectly.builtin.effects.staminaregen;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.registry.AbilityContext;
import com.riprod.effectly.core.effects.registry.AbilityHandler;

import javax.annotation.Nonnull;

public final class StaminaRegenHandler implements AbilityHandler {

    public static final String ID = "stamina_regen";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        StaminaRegenComponent.register(registry);
        registry.registerSystem(new ConditionalStatSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        StaminaRegenComponent component = context.getComponents()
                .getComponent(context.getRef(), StaminaRegenComponent.getComponentType());
        if (component == null) component = new StaminaRegenComponent();
        context.getComponents().putComponent(context.getRef(), StaminaRegenComponent.getComponentType(), component);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().tryRemoveComponent(context.getRef(), StaminaRegenComponent.getComponentType());
    }
}
