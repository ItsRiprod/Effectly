package com.riprod.effectly.core;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.riprod.effectly.ability.AbilityConditionSpec;
import com.riprod.effectly.ability.AbilityType;
import com.riprod.effectly.ability.AbilityValue;
import com.riprod.effectly.core.asset.AbilityAsset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityEntry {

    @Nonnull
    public static final BuilderCodec<AbilityEntry> CODEC = BuilderCodec
            .builder(AbilityEntry.class, AbilityEntry::new)
            .append(new KeyedCodec<>("Grants", new MapCodec<>(AbilityGrant.CODEC, LinkedHashMap::new, false)),
                    (entry, v) -> entry.grants = v == null ? new LinkedHashMap<>() : new LinkedHashMap<>(v),
                    AbilityEntry::persistentGrants)
            .documentation("Grant source id to the value and conditions that source contributes")
            .add()
            .build();

    private Map<String, AbilityGrant> grants = new LinkedHashMap<>();

    private AbilityEntry() {
    }

    public AbilityEntry(@Nonnull String sourceId, @Nonnull AbilityGrant grant) {
        this.grants.put(sourceId, grant);
    }

    @Nullable
    private Map<String, AbilityGrant> persistentGrants() {
        Map<String, AbilityGrant> out = null;
        for (Map.Entry<String, AbilityGrant> grant : grants.entrySet()) {
            if (!grant.getValue().isPersistent()) continue;
            if (out == null) out = new LinkedHashMap<>();
            out.put(grant.getKey(), grant.getValue());
        }
        return out;
    }

    @Nonnull
    public Map<String, AbilityGrant> getGrants() {
        return grants;
    }

    @Nullable
    public AbilityGrant getGrant(@Nonnull String sourceId) {
        return grants.get(sourceId);
    }

    public void putGrant(@Nonnull String sourceId, @Nonnull AbilityGrant grant) {
        grants.put(sourceId, grant);
    }

    @Nullable
    public AbilityGrant removeGrant(@Nonnull String sourceId) {
        return grants.remove(sourceId);
    }

    public boolean isEmpty() {
        return grants.isEmpty();
    }

    public boolean hasPersistentGrant() {
        for (AbilityGrant grant : grants.values()) {
            if (grant.isPersistent()) return true;
        }
        return false;
    }

    public double getValue() {
        double best = 0.0;
        boolean any = false;
        for (AbilityGrant grant : grants.values()) {
            if (!any || grant.getValue() > best) best = grant.getValue();
            any = true;
        }
        return best;
    }

    @Nonnull
    public List<AbilityConditionSpec> getConditions() {
        AbilityGrant api = grants.get(AbilitySources.API);
        return api != null ? api.getConditions() : List.of();
    }

    @Nonnull
    public AbilityEntry copy() {
        AbilityEntry copy = new AbilityEntry();
        for (Map.Entry<String, AbilityGrant> grant : grants.entrySet()) {
            copy.grants.put(grant.getKey(), grant.getValue().copy());
        }
        return copy;
    }

    @Nonnull
    public AbilityValue toValue(@Nonnull String abilityId) {
        return toValue(abilityId, getValue());
    }

    @Nonnull
    public static AbilityValue toValue(@Nonnull String abilityId, double value) {
        AbilityAsset asset = AbilityAsset.getAssetMap().getAsset(abilityId);
        if (asset != null && asset.getType() == AbilityType.BINARY) {
            return new AbilityValue(value != 0.0);
        }
        return new AbilityValue(value);
    }
}
