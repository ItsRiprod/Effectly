package com.riprod.effectly.core.abilities.registry;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.effects.registry.EffectAsset;
import com.riprod.effectly.core.effects.registry.EffectHandlerConfig;
import com.riprod.effectly.core.effects.registry.EffectHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityHandlerRegistry {

    private static final Map<String, EffectHandler> HANDLERS = new LinkedHashMap<>();

    private AbilityHandlerRegistry() {}

    public static void reset() {
        HANDLERS.clear();
    }

    public static void register(@Nonnull EffectHandler handler) {
        EffectHandler existing = HANDLERS.putIfAbsent(handler.getId(), handler);
        if (existing != null && existing.getClass() != handler.getClass()) {
            throw new IllegalArgumentException("duplicate ability handler id: " + handler.getId());
        }
        if (existing != null) HANDLERS.put(handler.getId(), handler);

        EffectHandler.ConfigBinding<? extends EffectHandlerConfig> binding = handler.getConfigBinding();
        if (binding != null) {
            EffectHandlerConfig.CODEC.register(handler.getId(), binding.type(), binding.codec());
        }
    }

    public static void installAll(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        for (EffectHandler handler : HANDLERS.values()) {
            handler.install(registry);
        }
    }

    @Nullable
    public static EffectHandler get(@Nonnull String handlerId) {
        return HANDLERS.get(handlerId);
    }

    @Nullable
    public static EffectHandler forAbility(@Nonnull String abilityId) {
        EffectAsset asset = EffectAsset.getAssetMap().getAsset(abilityId);
        if (asset == null) return null;
        return HANDLERS.get(asset.getHandler());
    }

    public static boolean holdsAnyFor(@Nonnull AbilityContext context, @Nonnull EffectHandler handler) {
        return !ResolvedAbilityComponent
                .forHandler(context.getRef(), context.getComponents(), handler.getId())
                .isEmpty();
    }

    @Nonnull
    public static Collection<EffectHandler> all() {
        return Collections.unmodifiableCollection(HANDLERS.values());
    }

    @Nonnull
    public static Collection<String> ids() {
        return Collections.unmodifiableCollection(HANDLERS.keySet());
    }
}
