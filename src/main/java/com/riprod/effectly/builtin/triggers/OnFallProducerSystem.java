package com.riprod.effectly.builtin.triggers;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.actions.ActionDispatch;
import com.riprod.effectly.core.actions.component.ActionHolderComponent;
import com.riprod.effectly.core.damage.DamageModifierPipelineDependencies;

import java.util.Set;
import javax.annotation.Nonnull;

public final class OnFallProducerSystem extends DamageEventSystem {

    public static final String TRIGGER = "OnFall";

    private static final String FALL_CAUSE_ID = "Fall";

    private static final Query<EntityStore> QUERY = ActionHolderComponent.getComponentType();

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
        DamageCause cause = DamageCause.getAssetMap().getAsset(damage.getDamageCauseIndex());
        if (cause == null || !FALL_CAUSE_ID.equals(cause.getId())) return;

        float amount = damage.getAmount();
        if (amount <= 0f) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null) return;

        ActionHolderComponent holder = archetypeChunk.getComponent(index, ActionHolderComponent.getComponentType());
        if (holder == null) return;

        TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        if (transform == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        ActionDispatch.fire(TRIGGER, ref, store, commandBuffer, world, playerRef.getUuid(),
                holder, amount, transform.getPosition(), null, damage);
    }
}
