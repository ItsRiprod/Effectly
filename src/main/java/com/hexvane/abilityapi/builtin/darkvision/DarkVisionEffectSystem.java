package com.hexvane.abilityapi.builtin.darkvision;

import com.hexvane.abilityapi.config.AbilityApiConfig;
import com.hexvane.abilityapi.systems.AbilityConditionService;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class DarkVisionEffectSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return DarkVisionComponent.getComponentType();
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        DarkVisionComponent state = archetypeChunk.getComponent(index, DarkVisionComponent.getComponentType());
        if (state == null) return;

        AbilityApiConfig config = AbilityApiConfig.get();
        if (!state.due(dt, config.getDarkVisionCheckSeconds())) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        EntityEffect effect = EntityEffect.getAssetMap().getAsset(config.getDarkVisionEffectId());
        if (effect == null) return;

        int effectIndex = EntityEffect.getAssetMap().getIndex(effect.getId());
        if (effectIndex == Integer.MIN_VALUE) return;

        EffectControllerComponent controller = store.getComponent(ref, EffectControllerComponent.getComponentType());
        if (controller == null) return;

        boolean active = AbilityConditionService.isAbilityActive(
                ref, store, world, playerRef.getUuid(), DarkVisionHandler.ID);
        boolean applied = controller.getActiveEffects().containsKey(effectIndex);

        if (active && !applied) {
            controller.addEffect(ref, effectIndex, effect, commandBuffer);
        } else if (!active && applied) {
            controller.removeEffect(ref, effectIndex, commandBuffer);
        }
    }
}
