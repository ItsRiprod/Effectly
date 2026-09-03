package com.riprod.effectly.core.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.actions.component.ActionHolderComponent;
import com.riprod.effectly.core.actions.effects.ActionEffects;
import com.riprod.effectly.core.effects.registry.EffectAsset;
import com.riprod.effectly.core.utils.AbilityConditionUtils;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ActionDispatch {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

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

            // an asset reload can shorten or reorder a trigger's actions while the resolved view
            // still holds the old index, so this is reachable in normal operation and must not throw
            Action[] actions = asset.actionsFor(triggerId);
            int actionIndex = entry.actionIndex();
            if (actionIndex < 0 || actionIndex >= actions.length) {
                LOGGER.atFine().log(
                        "Action index %s of ability '%s' is out of bounds (%s actions) after a reload; skipping",
                        actionIndex, entry.abilityId(), actions.length);
                continue;
            }
            Action action = actions[actionIndex];

            if (!action.thresholdMet(context)) continue;

            double value = AbilityConditionUtils.activeValue(
                    holder, store, context.getWorld(), entry.abilityId(), targetRef);
            if (!AbilityConditionUtils.isActive(value)) continue;

            String cooldownKey = null;
            if (action.getCooldown() > 0) {
                cooldownKey = entry.abilityId() + "#" + triggerId + "#" + entry.actionIndex();
                if (holderComponent.isOnCooldown(cooldownKey)) continue;
            }

            if (!action.execute(context, value)) continue;
            if (cooldownKey != null) {
                holderComponent.startCooldown(cooldownKey, action.getCooldown());
            }
            ActionEffects effects = action.getEffects();
            if (effects != null) effects.play(holder, context);
        }
    }
}
