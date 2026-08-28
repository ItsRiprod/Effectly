package com.riprod.effectly.core.abilities.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.effects.registry.EffectAsset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Transient view of a player's roster grouped by the handler that implements each ability, with the
 * asset index resolved. Rebuilt whenever the roster changes, never persisted - asset indexes are
 * assigned in load order and are not stable across restarts.
 */
public final class ResolvedAbilityComponent implements Component<EntityStore> {

    public record Resolved(@Nonnull String abilityId, int assetIndex) {}

    private static ComponentType<EntityStore, ResolvedAbilityComponent> componentType;

    private Map<String, List<Resolved>> byHandler = Map.of();

    @Nonnull
    public static ComponentType<EntityStore, ResolvedAbilityComponent> getComponentType() {
        return componentType;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        if (componentType == null) {
            componentType = registry.registerComponent(
                    ResolvedAbilityComponent.class, ResolvedAbilityComponent::new);
        }
    }

    @Nullable
    public static ResolvedAbilityComponent of(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store) {
        return componentType != null ? store.getComponent(ref, componentType) : null;
    }

    /**
     * Abilities on this entity implemented by the given handler. A handler may back several assets,
     * so callers must not assume the handler id is also an ability id.
     */
    @Nonnull
    public static List<Resolved> forHandler(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull String handlerId) {
        ResolvedAbilityComponent resolved = of(ref, store);
        return resolved == null ? List.of() : resolved.forHandler(handlerId);
    }

    @Nonnull
    public List<Resolved> forHandler(@Nonnull String handlerId) {
        return byHandler.getOrDefault(handlerId, List.of());
    }

    public boolean isEmpty() {
        return byHandler.isEmpty();
    }

    @Nonnull
    public static Map<String, List<Resolved>> resolve(@Nonnull AbilityComponent roster) {
        Map<String, List<Resolved>> out = new HashMap<>();
        for (String abilityId : roster.getAbilities().keySet()) {
            EffectAsset asset = EffectAsset.getAssetMap().getAsset(abilityId);
            if (asset == null || !asset.isEnabled()) continue;
            out.computeIfAbsent(asset.getHandler(), id -> new ArrayList<>())
                    .add(new Resolved(abilityId, EffectAsset.indexOf(abilityId)));
        }
        return out;
    }

    public void setResolved(@Nonnull Map<String, List<Resolved>> resolved) {
        this.byHandler = resolved;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        ResolvedAbilityComponent copy = new ResolvedAbilityComponent();
        copy.byHandler = new HashMap<>(this.byHandler);
        return copy;
    }
}
