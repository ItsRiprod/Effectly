package com.riprod.effectly.builtin.triggers;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.builtin.capabilities.MagnitudeCapability;
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

public final class OnCraftProducerSystem extends EntityEventSystem<EntityStore, CraftRecipeEvent.Post> {

    public static final String TRIGGER = "OnCraft";

    public static final Set<CapabilityType<?>> PROVIDES = Set.of(
            SelfCapability.TYPE,
            PositionCapability.TYPE,
            MagnitudeCapability.TYPE);

    public OnCraftProducerSystem() {
        super(CraftRecipeEvent.Post.class);
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
            @Nonnull CraftRecipeEvent.Post event) {
        if (event.isCancelled()) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        if (archetypeChunk.getComponent(index, PlayerRef.getComponentType()) == null) return;

        ActionHolderComponent holder = archetypeChunk.getComponent(
                index, ActionHolderComponent.getComponentType());
        if (holder == null) return;

        ResolvedAbilityComponent resolved = ResolvedAbilityComponent.of(ref, store);
        if (resolved == null || !resolved.hasTrigger(TRIGGER)) return;

        TransformComponent transform = archetypeChunk.getComponent(
                index, TransformComponent.getComponentType());
        if (transform == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        ActionContext context = ActionContext.builder(commandBuffer, world)
                .with(SelfCapability.TYPE, new SelfCapability(ref))
                .with(PositionCapability.TYPE, new PositionCapability(transform.getPosition()))
                .with(MagnitudeCapability.TYPE, new MagnitudeCapability(event.getQuantity()))
                .build();

        ActionDispatch.fire(TRIGGER, ref, holder, context, null);
    }
}
