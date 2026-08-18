package com.riprod.effectly.builtin.effects.movespeed;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.AbilityContext;

import javax.annotation.Nonnull;

public final class MoveSpeedSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return MoveSpeedComponent.getComponentType();
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        MoveSpeedComponent component = archetypeChunk.getComponent(index, MoveSpeedComponent.getComponentType());
        if (component == null) return;

        float interval = component
                .configOrDefault(MoveSpeedConfig.class, MoveSpeedConfig.DEFAULTS)
                .getReassertSeconds();
        if (component.addTime(dt) < interval) return;
        component.consumeTime(interval);

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        MoveSpeedHandler.apply(new AbilityContext(ref, commandBuffer, world, playerRef), component);
    }
}
