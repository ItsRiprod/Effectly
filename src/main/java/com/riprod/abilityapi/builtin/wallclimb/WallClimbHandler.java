package com.riprod.abilityapi.builtin.wallclimb;

import com.riprod.abilityapi.core.AbilityContext;
import com.riprod.abilityapi.core.AbilityEntry;
import com.riprod.abilityapi.core.AbilityHandler;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class WallClimbHandler implements AbilityHandler {

    public static final String ID = "wall_climb";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<WallClimbConfig> getConfigBinding() {
        return ConfigBinding.of(WallClimbConfig.class, WallClimbConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        WallClimbComponent.register(registry);
        registry.registerSystem(new WallClimbSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        WallClimbComponent component = context.getComponents().getComponent(context.getRef(), WallClimbComponent.getComponentType());
        if (component == null) {
            component = new WallClimbComponent();
            context.getComponents().putComponent(context.getRef(), WallClimbComponent.getComponentType(), component);
        }
        component.bind(abilityId);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().removeComponent(context.getRef(), WallClimbComponent.getComponentType());
    }
}
