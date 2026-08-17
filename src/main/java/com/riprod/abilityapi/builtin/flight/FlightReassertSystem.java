package com.riprod.abilityapi.builtin.flight;

import com.riprod.abilityapi.core.AbilityContext;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class FlightReassertSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return FlightComponent.getComponentType();
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        FlightComponent state = archetypeChunk.getComponent(index, FlightComponent.getComponentType());
        if (state == null) return;

        float interval = state.configOrDefault(FlightConfig.class, FlightConfig.DEFAULTS).getReassertSeconds();
        if (state.addTime(dt) < interval) return;
        state.consumeTime(interval);

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        MovementManager movementManager = store.getComponent(ref, EntityModule.get().getMovementManagerComponentType());
        if (movementManager == null) return;

        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        AbilityContext context = new AbilityContext(ref, commandBuffer, world, playerRef);
        FlightHandler.applyCanFly(context, FlightHandler.isActive(context));
    }
}
