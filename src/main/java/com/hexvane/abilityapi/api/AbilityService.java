package com.hexvane.abilityapi.api;

import com.hexvane.abilityapi.ability.AbilityConditionSpec;
import com.hexvane.abilityapi.core.AbilityMutations;
import com.hexvane.abilityapi.systems.AbilityStatService;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AbilityService {

    private AbilityService() {
    }

    public static void setAbility(@Nonnull UUID playerId, @Nonnull String abilityId, @Nonnull Object value) {
        setAbility(playerId, abilityId, value, null);
    }

    public static void setAbility(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull Object value,
            @Nullable List<AbilityConditionSpec> conditions) {
        AbilityMutations.grant(playerId, abilityId, toDouble(value), conditions);
    }

    public static void setConditions(@Nonnull UUID playerId, @Nonnull String abilityId, @Nonnull List<AbilityConditionSpec> conditions) {
        AbilityMutations.setConditions(playerId, abilityId, conditions);
    }

    public static void removeAbility(@Nonnull UUID playerId, @Nonnull String abilityId) {
        AbilityMutations.revoke(playerId, abilityId);
    }

    public static void applyForPlayer(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world
    ) {
        AbilityMutations.applyAll(ref, store, world);
        AbilityStatService.applyForPlayer(ref, store, world);
    }

    private static double toDouble(@Nonnull Object value) {
        if (value instanceof Boolean flag) return flag ? 1.0 : 0.0;
        if (value instanceof Number number) return number.doubleValue();
        return 0.0;
    }
}
