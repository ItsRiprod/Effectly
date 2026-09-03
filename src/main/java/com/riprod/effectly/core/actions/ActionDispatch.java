package com.riprod.effectly.core.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.actions.component.ActionHolderComponent;
import com.riprod.effectly.core.effects.registry.EffectAsset;
import com.riprod.effectly.core.utils.AbilityConditionUtils;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ActionDispatch {

    private ActionDispatch() {}

    public static void fire(
            @Nonnull String triggerId,
            @Nonnull Ref<EntityStore> holder,
            @Nonnull ActionHolderComponent holderComponent,
            @Nonnull ActionContext context,
            @Nullable Ref<EntityStore> targetRef) {
        var store = context.getEntityStore();
        List<ResolvedAbilityComponent.ResolvedAction> entries =
                ResolvedAbilityComponent.forTrigger(holder, store, triggerId);
        if (entries.isEmpty()) return;

        for (ResolvedAbilityComponent.ResolvedAction entry : entries) {
            EffectAsset asset = EffectAsset.byIndex(entry.assetIndex());
            if (asset == null) continue;

            List<Action> actions = asset.getActions();
            if (entry.actionIndex() >= actions.size()) continue;
            Action action = actions.get(entry.actionIndex());

            // a reload can rebind an action to another trigger before the resolved view refreshes
            if (!triggerId.equals(action.getTrigger())) continue;
            if (!action.thresholdMet(context)) continue;

            double value = AbilityConditionUtils.activeValue(
                    holder, store, context.getWorld(), entry.abilityId(), targetRef);
            if (!AbilityConditionUtils.isActive(value)) continue;

            String cooldownKey = null;
            if (action.getCooldown() > 0) {
                cooldownKey = entry.abilityId() + "#" + entry.actionIndex();
                if (holderComponent.isOnCooldown(cooldownKey)) continue;
            }

            if (action.execute(context, value) && cooldownKey != null) {
                holderComponent.startCooldown(cooldownKey, action.getCooldown());
            }
        }
    }
}
