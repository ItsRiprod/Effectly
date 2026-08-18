package com.hexvane.abilityapi.api;

import com.hexvane.abilityapi.ability.AbilityConditionSpec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated
public final class AbilityService {

    private AbilityService() {
    }

    public static void setAbility(@Nonnull UUID playerId, @Nonnull String abilityId, @Nonnull Object value) {
        com.riprod.effectly.api.AbilityService.setAbility(playerId, abilityId, value);
    }

    public static void setAbility(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull Object value,
            @Nullable List<AbilityConditionSpec> conditions) {
        com.riprod.effectly.api.AbilityService.setAbility(playerId, abilityId, value, convert(conditions));
    }

    public static void setConditions(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull List<AbilityConditionSpec> conditions) {
        List<com.riprod.effectly.ability.AbilityConditionSpec> converted = convert(conditions);
        com.riprod.effectly.api.AbilityService.setConditions(
                playerId, abilityId, converted != null ? converted : List.of());
    }

    public static void removeAbility(@Nonnull UUID playerId, @Nonnull String abilityId) {
        com.riprod.effectly.api.AbilityService.removeAbility(playerId, abilityId);
    }

    public static void applyForPlayer(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world) {
        com.riprod.effectly.api.AbilityService.applyForPlayer(ref, store, world);
    }

    @Nullable
    private static List<com.riprod.effectly.ability.AbilityConditionSpec> convert(
            @Nullable List<AbilityConditionSpec> conditions) {
        if (conditions == null) return null;
        List<com.riprod.effectly.ability.AbilityConditionSpec> out = new ArrayList<>(conditions.size());
        for (AbilityConditionSpec spec : conditions) {
            if (spec == null) continue;
            out.add(new com.riprod.effectly.ability.AbilityConditionSpec(
                    spec.type(), spec.param(), spec.zoneIds()));
        }
        return out;
    }
}
