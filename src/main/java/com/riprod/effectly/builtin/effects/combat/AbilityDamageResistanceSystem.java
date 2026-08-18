package com.riprod.effectly.builtin.effects.combat;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.AbilityRoster;
import com.riprod.effectly.core.asset.AbilityAsset;
import com.riprod.effectly.systems.AbilityConditionService;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.hypixel.hytale.component.SystemGroup;

/**
 * Applies per-damage-type resistance or weakness from ability values.
 * The damage cause is declared by each ability's ResistanceConfig.
 * Value semantics: 0 = normal; 0 to 1 = resistance (reduced damage); -1 to 0 = weakness (increased damage).
 * Formula: damage *= (1 - value)
 * <p>
 * A resistance also covers every cause derived from it via DamageCause.getInherits(), so a resistance
 * on Elemental applies to Fire. The most specific active resistance wins; a value of 0 is active and
 * therefore shadows any broader resistance.
 */
public class AbilityDamageResistanceSystem extends DamageEventSystem {
    private static final Query<EntityStore> QUERY = ResistanceComponent.getComponentType();
    private static final int MAX_CHAIN_DEPTH = 8;

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
        return QUERY;
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull com.hypixel.hytale.component.Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull Damage damage) {
        Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());
        if (playerComponent == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRefComponent == null) return;

        DamageCause damageCause = DamageCause.getAssetMap().getAsset(damage.getDamageCauseIndex());
        if (damageCause == null) return;

        var ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        Double value = resistanceFor(ref, store, world, playerRefComponent.getUuid(), damageCause);
        if (value == null || value == 0) return;

        float currentAmount = damage.getAmount();
        float newAmount = (float) Math.max(0.0, currentAmount * (1.0 - value));
        damage.setAmount(newAmount);
    }

    @Nullable
    private static Double resistanceFor(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world,
            @Nonnull java.util.UUID playerId,
            @Nonnull DamageCause cause) {
        AbilityRoster roster = AbilityRoster.of(ref, store);
        if (roster == null || roster.isEmpty()) return null;

        DamageCause current = cause;
        for (int depth = 0; current != null && depth < MAX_CHAIN_DEPTH; depth++) {
            String causeId = current.getId();
            if (causeId == null || causeId.isBlank()) return null;

            Double best = bestAt(ref, store, world, playerId, roster, causeId);
            if (best != null) return best;

            String inherits = current.getInherits();
            if (inherits == null || inherits.isBlank()) return null;
            current = DamageCause.getAssetMap().getAsset(inherits);
        }
        return null;
    }

    @Nullable
    private static Double bestAt(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world,
            @Nonnull java.util.UUID playerId,
            @Nonnull AbilityRoster roster,
            @Nonnull String causeId) {
        Double best = null;
        for (String abilityId : roster.getAbilities().keySet()) {
            AbilityAsset asset = AbilityAsset.getAssetMap().getAsset(abilityId);
            if (asset == null || !asset.isEnabled()) continue;
            if (!(asset.getHandlerConfig() instanceof ResistanceConfig config)) continue;
            if (!causeId.equalsIgnoreCase(config.getDamageCause())) continue;

            var abilityValue = AbilityConditionService.getActiveAbilityValue(ref, store, world, playerId, abilityId);
            if (abilityValue == null || !abilityValue.isPresent()) continue;
            if (!(abilityValue.getRaw() instanceof Number n)) continue;

            double value = n.doubleValue();
            if (!Double.isFinite(value)) continue;
            if (best == null || value > best) best = value;
        }
        return best;
    }
}
