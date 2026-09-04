package com.riprod.effectly.builtin.triggers;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.builtin.capabilities.DamageCapability;
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

public final class OnDeathProducerSystem extends DeathSystems.OnDeathSystem {

    public static final String TRIGGER = "OnDeath";

    public static final String TRIGGER_KILLED = "OnKilled";

    public static final String TRIGGER_KILL = "OnKill";

    public static final Set<CapabilityType<?>> PROVIDES = Set.of(
            SelfCapability.TYPE,
            PositionCapability.TYPE,
            DamageCapability.TYPE);

    public static final Set<CapabilityType<?>> PROVIDES_WITH_OTHER = Set.of(
            SelfCapability.TYPE,
            PositionCapability.TYPE,
            DamageCapability.TYPE,
            OtherEntityCapability.TYPE);

    private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(
            new SystemDependency<>(Order.AFTER, DeathSystems.ClearEntityEffects.class));

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return DEPENDENCIES;
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull DeathComponent component,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Damage deathInfo = component.getDeathInfo();
        if (deathInfo == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        Ref<EntityStore> killer = killerOf(deathInfo, ref);

        if (killer != null) {
            produce(ref, killer, deathInfo, world, store, commandBuffer, TRIGGER, TRIGGER_KILLED);
            produce(killer, ref, deathInfo, world, store, commandBuffer, TRIGGER_KILL);
        } else {
            produce(ref, null, deathInfo, world, store, commandBuffer, TRIGGER);
        }
    }

    private static void produce(
            @Nonnull Ref<EntityStore> holderRef,
            @Nullable Ref<EntityStore> other,
            @Nonnull Damage damage,
            @Nonnull World world,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull String... triggers) {
        if (store.getComponent(holderRef, PlayerRef.getComponentType()) == null) return;

        ActionHolderComponent holder = store.getComponent(holderRef, ActionHolderComponent.getComponentType());
        if (holder == null) return;

        ResolvedAbilityComponent resolved = ResolvedAbilityComponent.of(holderRef, store);
        if (resolved == null) return;

        boolean wantsAny = false;
        for (String trigger : triggers) {
            if (resolved.hasTrigger(trigger)) {
                wantsAny = true;
                break;
            }
        }
        if (!wantsAny) return;

        TransformComponent transform = store.getComponent(holderRef, TransformComponent.getComponentType());
        if (transform == null) return;

        ActionContext.Builder builder = ActionContext.builder(commandBuffer, world)
                .with(SelfCapability.TYPE, new SelfCapability(holderRef))
                .with(PositionCapability.TYPE, new PositionCapability(transform.getPosition()))
                .with(DamageCapability.TYPE, new DamageCapability(damage));
        if (other != null) {
            builder.with(OtherEntityCapability.TYPE, new OtherEntityCapability(other));
        }
        ActionContext context = builder.build();

        for (String trigger : triggers) {
            if (resolved.hasTrigger(trigger)) {
                ActionDispatch.fire(trigger, holderRef, holder, context, other);
            }
        }
    }

    @Nullable
    private static Ref<EntityStore> killerOf(@Nonnull Damage damage, @Nonnull Ref<EntityStore> decedent) {
        if (!(damage.getSource() instanceof Damage.EntitySource entitySource)) return null;
        Ref<EntityStore> killer = entitySource.getRef();
        if (killer == null || !killer.isValid() || killer.equals(decedent)) return null;
        return killer;
    }
}
