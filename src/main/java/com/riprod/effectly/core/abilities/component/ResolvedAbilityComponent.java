package com.riprod.effectly.core.abilities.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.effects.registry.EffectAsset;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
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

    public record ResolvedAction(@Nonnull String abilityId, int assetIndex, int actionIndex) {}

    private static ComponentType<EntityStore, ResolvedAbilityComponent> componentType;

    private Map<String, List<Resolved>> byHandler = Map.of();
    private Map<String, List<ResolvedAction>> byTrigger = Map.of();

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

    @Nonnull
    public static List<ResolvedAction> forTrigger(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull String triggerId) {
        ResolvedAbilityComponent resolved = of(ref, store);
        return resolved == null ? List.of() : resolved.forTrigger(triggerId);
    }

    @Nonnull
    public List<ResolvedAction> forTrigger(@Nonnull String triggerId) {
        return byTrigger.getOrDefault(triggerId, List.of());
    }

    public boolean hasTrigger(@Nonnull String triggerId) {
        return byTrigger.containsKey(triggerId);
    }

    public boolean hasActions() {
        return !byTrigger.isEmpty();
    }

    public boolean isEmpty() {
        return byHandler.isEmpty();
    }

    public void resolveFrom(@Nonnull AbilityComponent roster) {
        Map<String, List<Resolved>> handlers = new Object2ObjectOpenHashMap<>();
        Map<String, List<ResolvedAction>> triggers = new Object2ObjectOpenHashMap<>();
        for (String abilityId : roster.getAbilities().keySet()) {
            EffectAsset asset = EffectAsset.getAssetMap().getAsset(abilityId);
            if (asset == null || !asset.isEnabled()) continue;
            int assetIndex = EffectAsset.indexOf(abilityId);
            handlers.computeIfAbsent(asset.getHandler(), id -> new ArrayList<>())
                    .add(new Resolved(abilityId, assetIndex));
            List<Action> actions = asset.getActions();
            for (int i = 0; i < actions.size(); i++) {
                String trigger = actions.get(i).getTrigger();
                if (trigger == null) continue;
                triggers.computeIfAbsent(trigger, id -> new ArrayList<>())
                        .add(new ResolvedAction(abilityId, assetIndex, i));
            }
        }
        this.byHandler = handlers;
        this.byTrigger = triggers;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        ResolvedAbilityComponent copy = new ResolvedAbilityComponent();
        copy.byHandler = new Object2ObjectOpenHashMap<>(this.byHandler);
        copy.byTrigger = new Object2ObjectOpenHashMap<>(this.byTrigger);
        return copy;
    }
}
