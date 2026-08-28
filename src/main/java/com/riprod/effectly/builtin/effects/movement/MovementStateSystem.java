package com.riprod.effectly.builtin.effects.movement;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.FlyMode;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.effects.registry.EffectAsset;
import com.riprod.effectly.core.effects.registry.EffectHandlerConfig;
import com.riprod.effectly.core.utils.AbilityConditionUtils;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Owns every movement setting Effectly writes. Folds the entity's active movement abilities into a
 * desired state, compares it against what is currently applied, and pushes one update when they
 * differ - so reasserting after an engine-side reset is a side effect of the normal comparison
 * rather than a separate throttled system per ability.
 * <p>
 * Reverting is the same code path: when nothing wants a setting the fold yields the default, which
 * is written back. Once no movement ability remains and the defaults are restored, the marker
 * removes itself.
 */
public final class MovementStateSystem extends EntityTickingSystem<EntityStore> {

    private static final Query<EntityStore> QUERY = MovementStateComponent.getComponentType();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        MovementManager movementManager = store.getComponent(
                ref, EntityModule.get().getMovementManagerComponentType());
        if (movementManager == null) return;

        List<ResolvedAbilityComponent.Resolved> abilities =
                ResolvedAbilityComponent.forHandler(ref, store, MovementStateHandler.ID);

        boolean wantsFlight = false;
        float speedMultiplier = 1.0f;

        for (ResolvedAbilityComponent.Resolved ability : abilities) {
            MovementStateConfig config = configOf(ability);
            double value = AbilityConditionUtils.activeValue(
                    ref, store, world, playerRef.getUuid(), ability.abilityId());
            if (!AbilityConditionUtils.isActive(value)) continue;

            if (config.getField() == MovementStateConfig.Field.CAN_FLY) {
                if (value != 0.0) wantsFlight = true;
            } else {
                float factor = (float) value;
                if (factor > 0f) speedMultiplier *= factor;
            }
        }

        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        boolean creative = player != null && player.getGameMode() == GameMode.Creative;

        FlyMode targetFly = wantsFlight || creative ? FlyMode.Allowed : FlyMode.Disabled;
        float targetSpeed = movementManager.getDefaultSettings().baseSpeed * speedMultiplier;

        boolean changed = false;
        // Forced is never set here, so leave it to whatever set it rather than downgrading it
        FlyMode currentFly = movementManager.getSettings().fly;
        if (currentFly != FlyMode.Forced && currentFly != targetFly) {
            movementManager.getSettings().fly = targetFly;
            changed = true;
        }
        if (movementManager.getSettings().baseSpeed != targetSpeed) {
            movementManager.getSettings().baseSpeed = targetSpeed;
            changed = true;
        }
        if (changed) {
            movementManager.update(playerRef.getPacketHandler());
        }

        if (abilities.isEmpty() && !changed) {
            commandBuffer.tryRemoveComponent(ref, MovementStateComponent.getComponentType());
        }
    }

    @Nonnull
    private static MovementStateConfig configOf(@Nonnull ResolvedAbilityComponent.Resolved ability) {
        EffectAsset asset = EffectAsset.byIndex(ability.assetIndex());
        EffectHandlerConfig config = asset != null ? asset.getHandlerConfig() : null;
        return config instanceof MovementStateConfig movement ? movement : MovementStateConfig.DEFAULTS;
    }
}
