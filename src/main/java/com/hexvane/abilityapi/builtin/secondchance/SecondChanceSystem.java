package com.hexvane.abilityapi.builtin.secondchance;

import com.hexvane.abilityapi.config.AbilityApiConfig;
import com.hexvane.abilityapi.systems.AbilityConditionService;
import com.hexvane.abilityapi.builtin.combat.AbilityDamageResistanceSystem;
import com.hexvane.abilityapi.builtin.combat.DamageModifierPipelineDependencies;
import com.hexvane.abilityapi.builtin.survival.FallDamageImmunitySystem;
import com.hexvane.abilityapi.builtin.survival.InvulnerabilitySystem;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * When a player with second_chance would take lethal damage, prevents death and restores
 * them to 20% of max health. Enforces a 5-minute cooldown per player.
 */
public class SecondChanceSystem extends DamageEventSystem {
    private static final Query<EntityStore> QUERY = SecondChanceComponent.getComponentType();

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        Set<Dependency<EntityStore>> dependencies = new HashSet<>(
                DamageModifierPipelineDependencies.afterFilterBeforeApplyDamage());
        dependencies.add(new SystemDependency<>(Order.AFTER, AbilityDamageResistanceSystem.class));
        dependencies.add(new SystemDependency<>(Order.AFTER, InvulnerabilitySystem.class));
        dependencies.add(new SystemDependency<>(Order.AFTER, FallDamageImmunitySystem.class));
        return dependencies;
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
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull Damage damage) {
        Ref<EntityStore> targetRef = archetypeChunk.getReferenceTo(index);
        if (targetRef == null || !targetRef.isValid()) return;

        Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());
        if (playerComponent == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRefComponent == null) return;

        if (!AbilityConditionService.isAbilityActive(targetRef, store, world, playerRefComponent.getUuid(), SecondChanceHandler.ID)) {
            return;
        }
        SecondChanceComponent state = archetypeChunk.getComponent(index, SecondChanceComponent.getComponentType());
        if (state == null || state.onCooldown()) return;

        EntityStatMap statMap = archetypeChunk.getComponent(index, EntityStatMap.getComponentType());
        if (statMap == null) return;

        int healthIndex = DefaultEntityStatTypes.getHealth();
        if (healthIndex < 0 || healthIndex >= statMap.size()) return;

        EntityStatValue healthStat = statMap.get(healthIndex);
        if (healthStat == null) return;

        float currentHealth = healthStat.get();
        float maxHealth = healthStat.getMax();
        if (maxHealth <= 0f) return;

        float minHealth = healthStat.getMin();
        float lethalThreshold = currentHealth - minHealth;
        if (Math.round(damage.getAmount()) < lethalThreshold) {
            return;
        }

        // Trigger second chance: prevent death and restore to 20% max health
        float targetHealth = AbilityApiConfig.get().getSecondChanceRestorePercent() * maxHealth;
        float newDamage = currentHealth - targetHealth;

        if (newDamage <= 0f) {
            damage.setAmount(0f);
            float delta = targetHealth - currentHealth;
            statMap.addStatValue(healthIndex, delta);
        } else {
            damage.setAmount(newDamage);
        }

        state.startCooldown(AbilityApiConfig.get().getSecondChanceCooldownSeconds());
    }
}
