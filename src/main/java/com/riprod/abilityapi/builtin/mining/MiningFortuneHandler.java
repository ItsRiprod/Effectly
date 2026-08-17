package com.riprod.abilityapi.builtin.mining;

import com.riprod.abilityapi.core.AbilityContext;
import com.riprod.abilityapi.core.AbilityEntry;
import com.riprod.abilityapi.core.AbilityHandler;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class MiningFortuneHandler implements AbilityHandler {

    public static final String ID = "mining_fortune";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<MiningFortuneConfig> getConfigBinding() {
        return ConfigBinding.of(MiningFortuneConfig.class, MiningFortuneConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        MiningFortuneComponent.register(registry);
        registry.registerSystem(new MiningFortuneEventSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        MiningFortuneComponent component = new MiningFortuneComponent();
        component.bind(abilityId);
        context.getComponents().putComponent(context.getRef(), MiningFortuneComponent.getComponentType(), component);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().tryRemoveComponent(context.getRef(), MiningFortuneComponent.getComponentType());
    }
}
