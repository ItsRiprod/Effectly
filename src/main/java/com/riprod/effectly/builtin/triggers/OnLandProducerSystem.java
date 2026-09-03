package com.riprod.effectly.builtin.triggers;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerInput;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.builtin.capabilities.MagnitudeCapability;
import com.riprod.effectly.builtin.capabilities.PositionCapability;
import com.riprod.effectly.builtin.capabilities.SelfCapability;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.ActionDispatch;
import com.riprod.effectly.core.actions.capability.CapabilityType;
import com.riprod.effectly.core.actions.component.ActionHolderComponent;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class OnLandProducerSystem extends EntityTickingSystem<EntityStore> {

    public static final String TRIGGER = "OnLand";

    public static final Set<CapabilityType<?>> PROVIDES = Set.of(
            SelfCapability.TYPE,
            PositionCapability.TYPE,
            MagnitudeCapability.TYPE);

    private static final Query<EntityStore> QUERY = ActionHolderComponent.getComponentType();

    private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(
            new SystemDependency<>(Order.BEFORE, DamageSystems.FallDamagePlayers.class));

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getGatherDamageGroup();
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return DEPENDENCIES;
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        if (player == null) return;

        double distance = player.getCurrentFallDistance();
        if (distance <= 0) return;

        PlayerInput playerInput = archetypeChunk.getComponent(index, PlayerInput.getComponentType());
        if (playerInput == null) return;

        var queue = playerInput.getMovementUpdateQueue();
        for (int i = 0; i < queue.size(); i++) {
            if (!(queue.get(i) instanceof PlayerInput.SetMovementStates entry)) continue;
            if (!entry.movementStates().onGround) continue;

            if (entry.movementStates().inFluid) return;

            fire(index, archetypeChunk, store, commandBuffer, distance);
            return;
        }
    }

    private static void fire(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            double distance) {
        var ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        ActionHolderComponent holder = archetypeChunk.getComponent(index, ActionHolderComponent.getComponentType());
        if (holder == null) return;

        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null) return;

        TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        if (transform == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        ActionContext context = ActionContext.builder(commandBuffer, world)
                .with(SelfCapability.TYPE, new SelfCapability(ref))
                .with(PositionCapability.TYPE, new PositionCapability(transform.getPosition()))
                .with(MagnitudeCapability.TYPE, new MagnitudeCapability(distance))
                .build();

        ActionDispatch.fire(TRIGGER, ref, holder, context, null);
    }
}
