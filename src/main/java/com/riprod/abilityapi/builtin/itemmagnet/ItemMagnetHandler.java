package com.riprod.abilityapi.builtin.itemmagnet;

import com.riprod.abilityapi.core.AbilityContext;
import com.riprod.abilityapi.core.AbilityEntry;
import com.riprod.abilityapi.core.AbilityHandler;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class ItemMagnetHandler implements AbilityHandler {

    public static final String ID = "item_magnet";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<ItemMagnetConfig> getConfigBinding() {
        return ConfigBinding.of(ItemMagnetConfig.class, ItemMagnetConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        ItemMagnetComponent.register(registry);
        registry.registerSystem(new ItemMagnetSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        ItemMagnetComponent component = context.getComponents().getComponent(context.getRef(), ItemMagnetComponent.getComponentType());
        if (component == null) {
            component = new ItemMagnetComponent();
            context.getComponents().putComponent(context.getRef(), ItemMagnetComponent.getComponentType(), component);
        }
        component.bind(abilityId);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().removeComponent(context.getRef(), ItemMagnetComponent.getComponentType());
    }
}
