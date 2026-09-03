package com.riprod.effectly.builtin.effects.combat;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.utils.AbilityConditionUtils;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.hypixel.hytale.component.SystemGroup;

/**
 * Applies the attacker's punch_damage ability: multiplies PHYSICAL damage dealt by the player.
 * Query uses EntityStatMap when available so we run for any damage victim (player or NPC); else PlayerRef only.
 */
public class AbilityPunchDamageSystem extends DamageEventSystem {
    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        var dm = DamageModule.get();
        return dm != null ? dm.getFilterDamageGroup() : null;
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        var damageModule = DamageModule.get();
        var gatherGroup = damageModule != null ? damageModule.getGatherDamageGroup() : null;
        if (gatherGroup == null) return Set.of();
        return Set.of(new SystemGroupDependency<>(Order.AFTER, gatherGroup));
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
            @Nonnull com.hypixel.hytale.component.Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull Damage damage) {
        DamageCause cause = DamageCause.getAssetMap().getAsset(damage.getDamageCauseIndex());
        if (cause == null) return;

        String causeId = cause.getId();
        if (causeId == null || !causeId.equalsIgnoreCase("physical")) return;

        if (!(damage.getSource() instanceof Damage.EntitySource entitySource)) return;

        Ref<EntityStore> attackerRef = entitySource.getRef();
        if (attackerRef == null || !attackerRef.isValid()) return;

        if (!store.getArchetype(attackerRef).contains(PunchDamageComponent.getComponentType())) return;

        Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
        if (attackerPlayer == null) return;


        if (!ItemStack.isEmpty(InventoryComponent.getItemInHand(store, attackerRef))) return;

        PlayerRef attackerPlayerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
        if (attackerPlayerRef == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        Ref<EntityStore> targetRef = archetypeChunk.getReferenceTo(index);
        var punchDamageAbility = AbilityConditionUtils.bestActiveForHandler(
                attackerRef, store, world, attackerPlayerRef.getUuid(), PunchDamageHandler.ID, targetRef);
        if (punchDamageAbility == null) return;

        double multiplier = punchDamageAbility.value();
        if (multiplier == 1.0) return;

        float currentAmount = damage.getAmount();
        float newAmount = (float) (currentAmount * multiplier);
        if (newAmount < 0) newAmount = 0;
        damage.setAmount(newAmount);
    }
}
