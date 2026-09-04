package com.riprod.effectly.builtin.triggers;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.event.events.ecs.BreathingCheckEvent;
import com.hypixel.hytale.server.core.modules.entity.component.BreathingComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.builtin.capabilities.BreathCapability;
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

public final class OnBreathingProducerSystem extends EntityEventSystem<EntityStore, BreathingCheckEvent> {

    public static final String TRIGGER_SUBMERGE = "OnSubmerge";

    public static final String TRIGGER_ENTER_FLUID = "OnEnterFluid";

    public static final String TRIGGER_EXIT_FLUID = "OnExitFluid";

    public static final Set<CapabilityType<?>> PROVIDES = Set.of(
            SelfCapability.TYPE,
            PositionCapability.TYPE,
            BreathCapability.TYPE);

    public OnBreathingProducerSystem() {
        super(BreathingCheckEvent.class);
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
            @Nonnull BreathingCheckEvent event) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        if (archetypeChunk.getComponent(index, PlayerRef.getComponentType()) == null) return;

        ActionHolderComponent holder = archetypeChunk.getComponent(
                index, ActionHolderComponent.getComponentType());
        if (holder == null) return;

        ResolvedAbilityComponent resolved = ResolvedAbilityComponent.of(ref, store);
        if (resolved == null) return;

        boolean submerged = event.getFluidId() != Fluid.EMPTY_ID
                || event.getBreathingMaterial() != BlockMaterial.Empty;
        boolean inFluid = event.getFluidId() != Fluid.EMPTY_ID;

        BreathingComponent breathing = archetypeChunk.getComponent(
                index, BreathingComponent.getComponentType());
        boolean wasInFluid = breathing != null && breathing.getLastFluidId() != Fluid.EMPTY_ID;

        boolean fireSubmerge = submerged && resolved.hasTrigger(TRIGGER_SUBMERGE);
        boolean fireEnter = inFluid && !wasInFluid && resolved.hasTrigger(TRIGGER_ENTER_FLUID);
        boolean fireExit = !inFluid && wasInFluid && resolved.hasTrigger(TRIGGER_EXIT_FLUID);
        if (!fireSubmerge && !fireEnter && !fireExit) return;

        TransformComponent transform = archetypeChunk.getComponent(
                index, TransformComponent.getComponentType());
        if (transform == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        ActionContext context = ActionContext.builder(commandBuffer, world)
                .with(SelfCapability.TYPE, new SelfCapability(ref))
                .with(PositionCapability.TYPE, new PositionCapability(transform.getPosition()))
                .with(BreathCapability.TYPE, new BreathCapability(event))
                .build();

        if (fireSubmerge) {
            ActionDispatch.fire(TRIGGER_SUBMERGE, ref, holder, context, null);
        }
        if (fireEnter) {
            ActionDispatch.fire(TRIGGER_ENTER_FLUID, ref, holder, context, null);
        }
        if (fireExit) {
            ActionDispatch.fire(TRIGGER_EXIT_FLUID, ref, holder, context, null);
        }
    }
}
