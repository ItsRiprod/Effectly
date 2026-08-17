package com.hexvane.abilityapi.builtin.darkvision;

import com.hexvane.abilityapi.config.AbilityApiConfig;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class DarkVisionEffects {

    private DarkVisionEffects() {}

    public static void remove(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> components) {
        EntityEffect effect = EntityEffect.getAssetMap().getAsset(AbilityApiConfig.get().getDarkVisionEffectId());
        if (effect == null) return;

        int effectIndex = EntityEffect.getAssetMap().getIndex(effect.getId());
        if (effectIndex == Integer.MIN_VALUE) return;

        EffectControllerComponent controller = components.getComponent(ref, EffectControllerComponent.getComponentType());
        if (controller == null || !controller.getActiveEffects().containsKey(effectIndex)) return;

        controller.removeEffect(ref, effectIndex, components);
    }
}
