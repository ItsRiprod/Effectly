package com.riprod.effectly.builtin.triggers;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
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
import com.riprod.effectly.core.actions.capability.CapabilityType;
import com.riprod.effectly.core.actions.component.ActionHolderComponent;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class VictimDamageProducerSystem extends DamageEventSystem {

    public static final Set<CapabilityType<?>> PROVIDES = Set.of(
            SelfCapability.TYPE,
            PositionCapability.TYPE,
            MagnitudeCapability.TYPE,
            DamageCapability.TYPE);

    public static final Set<CapabilityType<?>> PROVIDES_WITH_ATTACKER = Set.of(
            SelfCapability.TYPE,
            PositionCapability.TYPE,
            MagnitudeCapability.TYPE,
            DamageCapability.TYPE,
            OtherEntityCapability.TYPE);

    private static final Query<EntityStore> QUERY = ActionHolderComponent.getComponentType();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
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

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        if (archetypeChunk.getComponent(index, PlayerRef.getComponentType()) == null) return;

        ActionHolderComponent holder = archetypeChunk.getComponent(
                index, ActionHolderComponent.getComponentType());
        if (holder == null) return;

        ResolvedAbilityComponent resolved = ResolvedAbilityComponent.of(ref, store);
        if (resolved == null || !wantsAny(resolved)) return;

        TransformComponent transform = archetypeChunk.getComponent(
                index, TransformComponent.getComponentType());
        if (transform == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        Ref<EntityStore> attacker = attackerOf(damage, ref);

        ActionContext.Builder builder = ActionContext.builder(commandBuffer, world)
                .with(SelfCapability.TYPE, new SelfCapability(ref))
                .with(PositionCapability.TYPE, new PositionCapability(transform.getPosition()))
                .with(MagnitudeCapability.TYPE, new MagnitudeCapability(amount))
                .with(DamageCapability.TYPE, new DamageCapability(damage));
        if (attacker != null) {
            builder.with(OtherEntityCapability.TYPE, new OtherEntityCapability(attacker));
        }

        dispatch(ref, holder, builder.build(), attacker, damage);
    }

    protected abstract boolean wantsAny(@Nonnull ResolvedAbilityComponent resolved);

    protected abstract void dispatch(
            @Nonnull Ref<EntityStore> holder,
            @Nonnull ActionHolderComponent holderComponent,
            @Nonnull ActionContext context,
            @Nullable Ref<EntityStore> attacker,
            @Nonnull Damage damage);

    @Nullable
    private static Ref<EntityStore> attackerOf(@Nonnull Damage damage, @Nonnull Ref<EntityStore> victim) {
        if (!(damage.getSource() instanceof Damage.EntitySource entitySource)) return null;
        Ref<EntityStore> attacker = entitySource.getRef();
        if (attacker == null || !attacker.isValid() || attacker.equals(victim)) return null;
        return attacker;
    }
}
