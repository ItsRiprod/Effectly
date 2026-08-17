package com.riprod.abilityapi.core;

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

public final class AbilityRoster implements Component<EntityStore> {

    public static final String COMPONENT_ID = "AbilityAPI:Roster";

    @Nonnull
    public static final BuilderCodec<AbilityRoster> CODEC = BuilderCodec
            .builder(AbilityRoster.class, AbilityRoster::new)
            .append(new KeyedCodec<>("Abilities", new MapCodec<>(AbilityEntry.CODEC, LinkedHashMap::new, false)),
                    (roster, v) -> roster.abilities = v == null ? new LinkedHashMap<>() : new LinkedHashMap<>(v),
                    AbilityRoster::persistentAbilities)
            .documentation("Granted ability ids to their value and conditions")
            .add()
            .build();

    private static ComponentType<EntityStore, AbilityRoster> COMPONENT_TYPE;

    private Map<String, AbilityEntry> abilities = new LinkedHashMap<>();

    @Nonnull
    public static ComponentType<EntityStore, AbilityRoster> getComponentType() {
        return COMPONENT_TYPE;
    }

    @Nullable
    public static AbilityRoster of(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store) {
        return COMPONENT_TYPE != null ? store.getComponent(ref, COMPONENT_TYPE) : null;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(AbilityRoster.class, COMPONENT_ID, CODEC);
    }

    @Nullable
    private Map<String, AbilityEntry> persistentAbilities() {
        Map<String, AbilityEntry> out = null;
        for (Map.Entry<String, AbilityEntry> entry : abilities.entrySet()) {
            if (!entry.getValue().hasPersistentGrant()) continue;
            if (out == null) out = new LinkedHashMap<>();
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
        AbilityRoster copy = new AbilityRoster();
        copy.abilities = new LinkedHashMap<>();
        for (Map.Entry<String, AbilityEntry> entry : abilities.entrySet()) {
            copy.abilities.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }
}
