package com.riprod.abilityapi.builtin.mining;

import com.riprod.abilityapi.core.AbilityContext;
import com.riprod.abilityapi.core.AbilityEntry;
import com.riprod.abilityapi.core.AbilityHandler;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class MiningHasteHandler implements AbilityHandler {

    public static final String ID = "mining_haste";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<MiningHasteConfig> getConfigBinding() {
        return ConfigBinding.of(MiningHasteConfig.class, MiningHasteConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        MiningHasteComponent.register(registry);
        registry.registerSystem(new MiningHasteEventSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        MiningHasteComponent component = new MiningHasteComponent();
        component.bind(abilityId);
        context.getComponents().putComponent(context.getRef(), MiningHasteComponent.getComponentType(), component);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().tryRemoveComponent(context.getRef(), MiningHasteComponent.getComponentType());
    }
}
