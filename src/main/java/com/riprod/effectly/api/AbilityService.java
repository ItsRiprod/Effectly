package com.riprod.effectly.api;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;
import com.riprod.effectly.core.utils.AbilityMutationUtils;

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
        AbilityMutationUtils.grant(playerId, abilityId, toDouble(value), conditions);
    }

    public static void setAbility(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull Object value,
            @Nullable List<AbilityConditionSpec> conditions,
            @Nonnull String sourceId) {
        AbilityMutationUtils.grant(playerId, abilityId, sourceId, toDouble(value), conditions, true);
    }

    public static void setConditions(@Nonnull UUID playerId, @Nonnull String abilityId, @Nonnull List<AbilityConditionSpec> conditions) {
        AbilityMutationUtils.setConditions(playerId, abilityId, conditions);
    }

    public static void setConditions(
            @Nonnull UUID playerId,
            @Nonnull String abilityId,
            @Nonnull List<AbilityConditionSpec> conditions,
            @Nonnull String sourceId) {
        AbilityMutationUtils.setConditions(playerId, abilityId, sourceId, conditions);
    }

    public static void removeAbility(@Nonnull UUID playerId, @Nonnull String abilityId) {
        AbilityMutationUtils.revoke(playerId, abilityId);
    }

    public static void removeAbility(@Nonnull UUID playerId, @Nonnull String abilityId, @Nonnull String sourceId) {
        AbilityMutationUtils.revoke(playerId, abilityId, sourceId);
    }

    public static void applyForPlayer(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull World world
    ) {
        AbilityMutationUtils.applyAll(ref, commandBuffer, world);
    }

    public static void applyForPlayer(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world
    ) {
        AbilityMutationUtils.applyAllLater(ref, world);
    }

    private static double toDouble(@Nonnull Object value) {
        if (value instanceof Boolean flag) return flag ? 1.0 : 0.0;
        if (value instanceof Number number) return number.doubleValue();
        return 0.0;
    }
}
