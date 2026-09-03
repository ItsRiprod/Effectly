package com.riprod.effectly.core.abilities.registry;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.effects.registry.EffectAsset;
import com.riprod.effectly.core.effects.registry.AbilityHandlerConfig;
import com.riprod.effectly.core.effects.registry.AbilityHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityHandlerRegistry {

    private static final Map<String, AbilityHandler> HANDLERS = new LinkedHashMap<>();

    private AbilityHandlerRegistry() {}

    public static void reset() {
        HANDLERS.clear();
    }

    public static void register(@Nonnull AbilityHandler handler) {
        AbilityHandler existing = HANDLERS.putIfAbsent(handler.getId(), handler);
        if (existing != null && existing.getClass() != handler.getClass()) {
            throw new IllegalArgumentException("duplicate ability handler id: " + handler.getId());
        }
        if (existing != null) HANDLERS.put(handler.getId(), handler);

        AbilityHandler.ConfigBinding<? extends AbilityHandlerConfig> binding = handler.getConfigBinding();
        if (binding != null) {
            AbilityHandlerConfig.CODEC.register(handler.getId(), binding.type(), binding.codec());
        }
    }

    public static void installAll(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        for (AbilityHandler handler : HANDLERS.values()) {
            handler.install(registry);
        }
    }

    @Nullable
    public static AbilityHandler get(@Nonnull String handlerId) {
        return HANDLERS.get(handlerId);
    }

    @Nullable
    public static AbilityHandler forAbility(@Nonnull String abilityId) {
        EffectAsset asset = EffectAsset.getAssetMap().getAsset(abilityId);
        if (asset == null) return null;
        return HANDLERS.get(asset.getHandler());
    }

    public static boolean holdsAnyFor(@Nonnull AbilityContext context, @Nonnull AbilityHandler handler) {
        return !ResolvedAbilityComponent
                .forHandler(context.getRef(), context.getComponents(), handler.getId())
                .isEmpty();
    }

    @Nonnull
    public static Collection<AbilityHandler> all() {
        return Collections.unmodifiableCollection(HANDLERS.values());
    }

    @Nonnull
    public static Collection<String> ids() {
        return Collections.unmodifiableCollection(HANDLERS.keySet());
    }
}
