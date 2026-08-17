package com.riprod.abilityapi.core;

import com.riprod.abilityapi.ability.AbilityConditionSpec;
import com.riprod.abilityapi.ability.AbilityValue;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityRosters {

    private AbilityRosters() {}

    @Nullable
    public static AbilityRoster byUuid(@Nonnull UUID playerId) {
        Universe universe = Universe.get();
        PlayerRef playerRef = universe != null ? universe.getPlayer(playerId) : null;
        if (playerRef == null || !playerRef.isValid()) return null;
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null || !ref.isValid()) return null;
        if (AbilityRoster.getComponentType() == null) return null;
        return ref.getStore().getComponentConcurrent(ref, AbilityRoster.getComponentType());
    }

    @Nonnull
    public static Map<String, AbilityValue> readAll(@Nonnull UUID playerId) {
        AbilityRoster roster = byUuid(playerId);
        if (roster == null || roster.isEmpty()) return Map.of();
        Map<String, AbilityValue> out = new LinkedHashMap<>();
        for (Map.Entry<String, AbilityEntry> granted : roster.getAbilities().entrySet()) {
            out.put(granted.getKey(), granted.getValue().toValue(granted.getKey()));
        }
        return out;
    }

    @Nonnull
    public static Map<String, Map<String, AbilityGrant>> readAllGrants(@Nonnull UUID playerId) {
        AbilityRoster roster = byUuid(playerId);
        if (roster == null || roster.isEmpty()) return Map.of();
        Map<String, Map<String, AbilityGrant>> out = new LinkedHashMap<>();
        for (Map.Entry<String, AbilityEntry> granted : roster.getAbilities().entrySet()) {
            out.put(granted.getKey(), new LinkedHashMap<>(granted.getValue().getGrants()));
        }
        return out;
    }



    @Nullable
    public static List<AbilityConditionSpec> readConditions(@Nonnull UUID playerId, @Nonnull String abilityId) {
        AbilityRoster roster = byUuid(playerId);
        AbilityEntry entry = roster != null ? roster.get(abilityId) : null;
        return entry != null ? List.copyOf(entry.getConditions()) : null;
    }
}
