package com.riprod.effectly.builtin.effects.secondchance;

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
import com.riprod.effectly.core.damage.DamageModifierPipelineDependencies;
import com.riprod.effectly.core.utils.AbilityConditionUtils;

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
        return DamageModifierPipelineDependencies.afterFilterBeforeApplyDamage();
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

        SecondChanceComponent state = archetypeChunk.getComponent(index, SecondChanceComponent.getComponentType());
        if (state == null || state.onCooldown()) return;

        var active = AbilityConditionUtils.bestActiveForHandler(
                targetRef, store, world, SecondChanceHandler.ID);
        if (active == null) return;

        SecondChanceConfig config = active.configOrDefault(SecondChanceConfig.class, SecondChanceConfig.DEFAULTS);

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
        if (damage.getAmount() < lethalThreshold) {
            return;
        }

        // Trigger second chance: prevent death and restore to 20% max health
        float targetHealth = config.getRestorePercent() * maxHealth;
        float newDamage = currentHealth - targetHealth;

        if (newDamage <= 0f) {
            damage.setAmount(0f);
            float delta = targetHealth - currentHealth;
            statMap.addStatValue(healthIndex, delta);
        } else {
            damage.setAmount(newDamage);
        }

        state.startCooldown(config.getCooldownSeconds());
    }
}
