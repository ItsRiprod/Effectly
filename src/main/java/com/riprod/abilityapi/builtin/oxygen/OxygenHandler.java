package com.riprod.abilityapi.builtin.oxygen;

import com.riprod.abilityapi.core.AbilityContext;
import com.riprod.abilityapi.core.AbilityEntry;
import com.riprod.abilityapi.core.AbilityHandler;
import com.riprod.abilityapi.core.stat.StatContributions;
import com.riprod.abilityapi.core.stat.StatPacingComponent;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class OxygenHandler implements AbilityHandler {

    public static final String ID = "oxygen";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<OxygenConfig> getConfigBinding() {
        return ConfigBinding.of(OxygenConfig.class, OxygenConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        OxygenComponent.register(registry);
        StatPacingComponent.register(registry);
        StatContributions.register(new OxygenContributor());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        OxygenComponent component = new OxygenComponent();
        component.bind(abilityId);
        context.getComponents().putComponent(context.getRef(), OxygenComponent.getComponentType(), component);
        StatContributions.onGrant(context);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().removeComponent(context.getRef(), OxygenComponent.getComponentType());
        StatContributions.onRevoke(context);
    }
}
