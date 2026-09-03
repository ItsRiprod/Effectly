package com.riprod.effectly.builtin.triggers;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.builtin.capabilities.DamageCapability;
import com.riprod.effectly.builtin.capabilities.MagnitudeCapability;
import com.riprod.effectly.builtin.capabilities.OtherEntityCapability;
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

public final class OnAttackProducerSystem extends DamageEventSystem {

    public static final String TRIGGER = "OnAttack";

    public static final Set<CapabilityType<?>> PROVIDES = Set.of(
            SelfCapability.TYPE,
            PositionCapability.TYPE,
            MagnitudeCapability.TYPE,
            DamageCapability.TYPE,
            OtherEntityCapability.TYPE);

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        DamageModule module = DamageModule.get();
        return module != null ? module.getFilterDamageGroup() : null;
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        DamageModule module = DamageModule.get();
        SystemGroup<EntityStore> gather = module != null ? module.getGatherDamageGroup() : null;
        if (gather == null) return Set.of();
        return Set.of(new SystemGroupDependency<>(Order.AFTER, gather));
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull Damage damage) {
        float amount = damage.getAmount();
        if (amount <= 0f) return;

        if (!(damage.getSource() instanceof Damage.EntitySource entitySource)) return;

        Ref<EntityStore> attacker = entitySource.getRef();
        if (attacker == null || !attacker.isValid()) return;

        Ref<EntityStore> victim = archetypeChunk.getReferenceTo(index);
        if (victim == null || !victim.isValid() || victim.equals(attacker)) return;

        if (!store.getArchetype(attacker).contains(ActionHolderComponent.getComponentType())) return;
        if (store.getComponent(attacker, PlayerRef.getComponentType()) == null) return;

        ActionHolderComponent holder = store.getComponent(
                attacker, ActionHolderComponent.getComponentType());
        if (holder == null) return;

        ResolvedAbilityComponent resolved = ResolvedAbilityComponent.of(attacker, store);
        if (resolved == null || !resolved.hasTrigger(TRIGGER)) return;

        TransformComponent transform = store.getComponent(
                attacker, TransformComponent.getComponentType());
        if (transform == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        ActionContext context = ActionContext.builder(commandBuffer, world)
                .with(SelfCapability.TYPE, new SelfCapability(attacker))
                .with(PositionCapability.TYPE, new PositionCapability(transform.getPosition()))
                .with(MagnitudeCapability.TYPE, new MagnitudeCapability(amount))
                .with(DamageCapability.TYPE, new DamageCapability(damage))
                .with(OtherEntityCapability.TYPE, new OtherEntityCapability(victim))
                .build();

        ActionDispatch.fire(TRIGGER, attacker, holder, context, victim);
    }
}
