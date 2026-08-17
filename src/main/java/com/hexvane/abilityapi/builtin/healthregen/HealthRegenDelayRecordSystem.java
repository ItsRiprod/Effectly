package com.hexvane.abilityapi.builtin.healthregen;

import com.hexvane.abilityapi.config.AbilityApiConfig;
import com.hexvane.abilityapi.builtin.combat.DamageModifierPipelineDependencies;
import com.hexvane.abilityapi.builtin.survival.FallDamageImmunitySystem;
import com.hexvane.abilityapi.builtin.survival.InvulnerabilitySystem;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Records last damage game time for any player who takes damage, so that
 * health_regen can enforce a delay before regen starts (same idea as stamina regen delay).
 */
public class HealthRegenDelayRecordSystem extends DamageEventSystem {
    private static final Query<EntityStore> QUERY = HealthRegenComponent.getComponentType();

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        Set<Dependency<EntityStore>> dependencies = new HashSet<>(
                DamageModifierPipelineDependencies.afterFilterBeforeApplyDamage());
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
        if (Math.round(damage.getAmount()) <= 0) return;

        Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());
        if (playerComponent == null) return;

        PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRefComponent == null) return;

        HealthRegenComponent state = archetypeChunk.getComponent(index, HealthRegenComponent.getComponentType());
        if (state == null) return;

        state.blockFor(AbilityApiConfig.get().getHealthRegenDelaySeconds());
    }
}
