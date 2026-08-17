package com.riprod.abilityapi.builtin.staminaregen;

import com.riprod.abilityapi.ability.AbilityValue;
import com.riprod.abilityapi.systems.AbilityConditionService;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.condition.Condition;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public class ConditionalStatSystem extends EntityTickingSystem<EntityStore> {
    private static final Query<EntityStore> QUERY = StaminaRegenComponent.getComponentType();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        World world = store.getExternalData().getWorld();
        if (world == null) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRefComponent == null) return;

        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) return;

        int staminaIndex = DefaultEntityStatTypes.getStamina();
        if (staminaIndex < 0 || staminaIndex == Integer.MIN_VALUE || staminaIndex >= statMap.size()) return;

        AbilityValue activeValue = AbilityConditionService.getActiveAbilityValue(
                ref, store, world, playerRefComponent.getUuid(), StaminaRegenHandler.ID);
        float multiplier = activeValue != null && activeValue.getRaw() instanceof Number n ? n.floatValue() : 0f;
        if (multiplier <= 1f) return;

        WorldTimeResource worldTime = store.getResource(WorldTimeResource.getResourceType());
        if (worldTime == null) return;

        float baseRate = activeRegenPerSecond(ref, store, staminaIndex, worldTime.getGameTime());
        if (baseRate <= 0f) return;

        float extra = (multiplier - 1f) * baseRate * dt;
        if (extra > 0f) {
            statMap.addStatValue(EntityStatMap.Predictable.SELF, staminaIndex, extra);
        }
    }

    private static float activeRegenPerSecond(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            int staminaIndex,
            @Nonnull Instant now) {
        EntityStatType staminaType = EntityStatType.getAssetMap().getAsset(staminaIndex);
        if (staminaType == null) return 0f;

        EntityStatType.Regenerating[] regenerating = staminaType.getRegenerating();
        if (regenerating == null) return 0f;

        for (EntityStatType.Regenerating entry : regenerating) {
            if (entry.getRegenType() != EntityStatType.Regenerating.RegenType.ADDITIVE) continue;
            if (entry.getAmount() <= 0f || entry.getInterval() <= 0f) continue;
            if (!Condition.allConditionsMet(store, ref, now, entry)) continue;
            return entry.getAmount() / entry.getInterval();
        }
        return 0f;
    }
}
