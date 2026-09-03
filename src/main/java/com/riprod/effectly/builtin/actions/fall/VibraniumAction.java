package com.riprod.effectly.builtin.actions.fall;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;

import javax.annotation.Nonnull;

public final class VibraniumAction extends Action {

    @Nonnull
    public static final String ID = "Vibranium";

    @Nonnull
    public static final BuilderCodec<VibraniumAction> CODEC = BuilderCodec
            .builder(VibraniumAction.class, VibraniumAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("ConversionRate", Codec.DOUBLE),
                    (action, v) -> action.conversionRate = v,
                    action -> action.conversionRate)
            .documentation("Fraction of the absorbed fall damage returned as healing, scaled "
                    + "by value")
            .add()
            .build();

    private double conversionRate = 0.5;

    @Override
    public boolean execute(@Nonnull ActionContext context) {
        var damage = context.getDamage();
        if (damage == null) return false;

        float landed = damage.getAmount();
        if (landed <= 0f) return false;

        damage.setAmount(0f);

        EntityStatMap statMap = context.getStore().getComponent(
                context.getHolderRef(), EntityStatMap.getComponentType());
        if (statMap == null) return true;

        float healed = (float) (landed * conversionRate * context.getValue());
        if (healed <= 0f) return true;

        statMap.addStatValue(DefaultEntityStatTypes.getHealth(), healed);
        return true;
    }
}
