package com.riprod.effectly.builtin.effects.secondchance;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class SecondChanceCooldownSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return SecondChanceComponent.getComponentType();
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        SecondChanceComponent state = archetypeChunk.getComponent(index, SecondChanceComponent.getComponentType());
        if (state == null) return;

        state.tickCooldown(dt);
        if (state.onCooldown()) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;
        if (!ResolvedAbilityComponent.forHandler(ref, store, SecondChanceHandler.ID).isEmpty()) return;

        commandBuffer.tryRemoveComponent(ref, SecondChanceComponent.getComponentType());
    }
}
