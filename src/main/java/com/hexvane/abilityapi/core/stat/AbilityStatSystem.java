package com.hexvane.abilityapi.core.stat;

import com.hexvane.abilityapi.config.AbilityApiConfig;
import com.hexvane.abilityapi.core.AbilityContext;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class AbilityStatSystem extends EntityTickingSystem<EntityStore> {

    private final Query<EntityStore> query = StatContributions.query();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        StatPacingComponent pacing = store.getComponent(ref, StatPacingComponent.getComponentType());
        if (pacing == null) return;

        MovementStatesComponent movementStates = store.getComponent(ref, MovementStatesComponent.getComponentType());
        boolean swimming = movementStates != null
                && movementStates.getMovementStates() != null
                && movementStates.getMovementStates().swimming;

        if (!pacing.due(dt, AbilityApiConfig.get().getStatReassertSeconds(), swimming)) return;

        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        AbilityStatApplier.apply(new AbilityContext(ref, commandBuffer, world, playerRef));
    }
}
