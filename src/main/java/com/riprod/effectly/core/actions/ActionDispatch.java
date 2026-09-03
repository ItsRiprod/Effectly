package com.riprod.effectly.core.actions;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.actions.component.ActionHolderComponent;
import com.riprod.effectly.core.effects.registry.EffectAsset;
import com.riprod.effectly.core.utils.AbilityConditionUtils;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3d;

public final class ActionDispatch {

    private ActionDispatch() {}

    public static void fire(
            @Nonnull String triggerId,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull World world,
            @Nonnull ActionHolderComponent holder,
            double magnitude,
            @Nonnull Vector3d position,
            @Nullable Ref<EntityStore> otherRef,
            @Nullable Damage damage) {
        List<ResolvedAbilityComponent.ResolvedAction> entries =
                ResolvedAbilityComponent.forTrigger(ref, store, triggerId);
        if (entries.isEmpty()) return;

        for (ResolvedAbilityComponent.ResolvedAction entry : entries) {
            EffectAsset asset = EffectAsset.byIndex(entry.assetIndex());
            if (asset == null) continue;

            List<Action> actions = asset.getActions();
            if (entry.actionIndex() >= actions.size()) continue;
            Action action = actions.get(entry.actionIndex());

            // a reload can rebind an action to another trigger before the resolved view refreshes
            if (!triggerId.equals(action.getTrigger())) continue;
            if (magnitude < action.getMinFallDistance()) continue;

            double value = AbilityConditionUtils.activeValue(
                    ref, store, world, entry.abilityId(), otherRef);
            if (!AbilityConditionUtils.isActive(value)) continue;

            String cooldownKey = null;
            if (action.getCooldown() > 0) {
                cooldownKey = entry.abilityId() + "#" + entry.actionIndex();
                if (holder.isOnCooldown(cooldownKey)) continue;
            }

            boolean fired = action.execute(new ActionContext(ref, store, commandBuffer, world,
                    value, magnitude, position, otherRef, damage));

            if (fired && cooldownKey != null) {
                holder.startCooldown(cooldownKey, action.getCooldown());
            }
        }
    }
}
