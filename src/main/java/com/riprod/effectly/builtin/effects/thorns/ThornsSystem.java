package com.riprod.effectly.builtin.effects.thorns;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.damage.DamageModifierPipelineDependencies;
import com.riprod.effectly.core.utils.AbilityConditionUtils;

import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Reflects a fraction of incoming damage back at whoever dealt it. The ability value is that
 * fraction, so 0.25 returns a quarter of what landed.
 * <p>
 * Victim-side: the query is the holder's own marker, and the attacker is read off the damage source.
 * Runs after the damage has been modified so the reflected amount is based on what actually landed
 * rather than what was originally proposed.
 */
public final class ThornsSystem extends DamageEventSystem {

    private static final Query<EntityStore> QUERY = ThornsComponent.getComponentType();

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
        float landed = damage.getAmount();
        if (landed <= 0f) return;

        if (!(damage.getSource() instanceof Damage.EntitySource entitySource)) return;

        Ref<EntityStore> attackerRef = entitySource.getRef();
        if (attackerRef == null || !attackerRef.isValid()) return;

        Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
        if (victimRef == null || !victimRef.isValid()) return;
        if (attackerRef.equals(victimRef)) return;

        PlayerRef victimPlayerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (victimPlayerRef == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        var active = AbilityConditionUtils.bestActiveForHandler(
                victimRef, store, world, victimPlayerRef.getUuid(), ThornsHandler.ID, attackerRef);
        if (active == null) return;

        ThornsConfig config = active.configOrDefault(ThornsConfig.class, ThornsConfig.DEFAULTS);

        DamageCause reflectedCause = DamageCause.getAssetMap().getAsset(config.getDamageCause());
        if (reflectedCause == null) return;


        if (reflectedCause.getId().equalsIgnoreCase(currentCauseId(damage))) return;

        float reflected = (float) (landed * active.value());
        if (reflected <= 0f) return;

        DamageSystems.executeDamage(attackerRef, commandBuffer,
                new Damage(new Damage.EntitySource(victimRef), reflectedCause, reflected));
    }

    @Nonnull
    private static String currentCauseId(@Nonnull Damage damage) {
        DamageCause cause = DamageCause.getAssetMap().getAsset(damage.getDamageCauseIndex());
        String id = cause != null ? cause.getId() : null;
        return id == null ? "" : id;
    }
}
