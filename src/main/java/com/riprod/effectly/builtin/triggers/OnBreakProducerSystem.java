package com.riprod.effectly.builtin.triggers;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.builtin.capabilities.PositionCapability;
import com.riprod.effectly.builtin.capabilities.SelfCapability;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.ActionDispatch;
import com.riprod.effectly.core.actions.capability.CapabilityType;
import com.riprod.effectly.core.actions.component.ActionHolderComponent;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class OnBreakProducerSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    public static final String TRIGGER = "OnBreak";

    public static final Set<CapabilityType<?>> PROVIDES = Set.of(
            SelfCapability.TYPE,
            PositionCapability.TYPE);

    public OnBreakProducerSystem() {
        super(BreakBlockEvent.class);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return ActionHolderComponent.getComponentType();
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull BreakBlockEvent event) {
        if (event.isCancelled()) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        if (archetypeChunk.getComponent(index, PlayerRef.getComponentType()) == null) return;

        ActionHolderComponent holder = archetypeChunk.getComponent(
                index, ActionHolderComponent.getComponentType());
        if (holder == null) return;

        ResolvedAbilityComponent resolved = ResolvedAbilityComponent.of(ref, store);
        if (resolved == null || !resolved.hasTrigger(TRIGGER)) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        ActionContext context = ActionContext.builder(commandBuffer, world)
                .with(SelfCapability.TYPE, new SelfCapability(ref))
                .with(PositionCapability.TYPE,
                        new PositionCapability(event.getTargetBlock(), event.getBlockType()))
                .build();

        ActionDispatch.fire(TRIGGER, ref, holder, context, null);
    }
}
