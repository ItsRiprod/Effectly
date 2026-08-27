package com.riprod.effectly.core.abilities.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public final class AbilityComponent implements Component<EntityStore> {

    public static final String ID = "Effectly:Roster";

    @Nonnull
    public static ComponentType<EntityStore, AbilityComponent> getComponentType() {
        return componentType;
    }

    @Nonnull
    public static final BuilderCodec<@NotNull AbilityComponent> CODEC = BuilderCodec
            .builder(AbilityComponent.class, AbilityComponent::new)
            .append(new KeyedCodec<>("Abilities", new MapCodec<>(AbilityEntry.CODEC, LinkedHashMap::new, false)),
                    (roster, v) -> roster.abilities = v == null ? new LinkedHashMap<>() : new LinkedHashMap<>(v),
                    AbilityComponent::persistentAbilities)
            .documentation("Granted ability ids to their value and conditions")
            .add()
            .build();

    private static ComponentType<EntityStore, AbilityComponent> componentType;

    private Map<String, AbilityEntry> abilities = new LinkedHashMap<>();

    @Nullable
    public static AbilityComponent of(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store) {
        return componentType != null ? store.getComponent(ref, componentType) : null;
    }

    public static void setComponentType(ComponentType<EntityStore, AbilityComponent> type) {
        componentType = type;
    }

    @Nullable
    private Map<String, AbilityEntry> persistentAbilities() {
        Map<String, AbilityEntry> out = null;
        for (Map.Entry<String, AbilityEntry> entry : abilities.entrySet()) {
            if (!entry.getValue().hasPersistentGrant())
                continue;
            if (out == null)
                out = new LinkedHashMap<>();
            out.put(entry.getKey(), entry.getValue());
        }
        return out;
    }

    @Nonnull
    public Map<String, AbilityEntry> getAbilities() {
        return abilities;
    }

    @Nullable
    public AbilityEntry get(@Nonnull String abilityId) {
        return abilities.get(abilityId);
    }

    public boolean has(@Nonnull String abilityId) {
        return abilities.containsKey(abilityId);
    }

    public void put(@Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        abilities.put(abilityId, entry);
    }

    @Nullable
    public AbilityEntry remove(@Nonnull String abilityId) {
        return abilities.remove(abilityId);
    }

    public boolean isEmpty() {
        return abilities.isEmpty();
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        AbilityComponent copy = new AbilityComponent();
        copy.abilities = new LinkedHashMap<>();
        for (Map.Entry<String, AbilityEntry> entry : abilities.entrySet()) {
            copy.abilities.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }
}
