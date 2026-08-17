package com.riprod.abilityapi.builtin.swimspeed;

import com.riprod.abilityapi.core.AbilityContext;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class SwimSpeedSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return SwimSpeedComponent.getComponentType();
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        SwimSpeedComponent component = archetypeChunk.getComponent(index, SwimSpeedComponent.getComponentType());
        if (component == null) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        AbilityContext context = new AbilityContext(ref, commandBuffer, world, playerRef);

        float interval = component
                .configOrDefault(SwimSpeedConfig.class, SwimSpeedConfig.DEFAULTS)
                .getReassertSeconds();
        if (!component.due(dt, interval, SwimSpeedHandler.isSwimming(context))) return;

        SwimSpeedHandler.apply(context, component);
    }
}
