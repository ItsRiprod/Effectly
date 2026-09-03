package com.riprod.effectly.builtin.actions.fall;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;

import javax.annotation.Nonnull;

public final class HeavyAction extends Action {

    @Nonnull
    public static final String ID = "Heavy";

    @Nonnull
    public static final BuilderCodec<HeavyAction> CODEC = BuilderCodec
            .builder(HeavyAction.class, HeavyAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("Reduction", Codec.DOUBLE),
                    (action, v) -> action.reduction = v,
                    action -> action.reduction)
            .documentation("Fraction of fall damage removed at value 1.0. Reduction * value of "
                    + "1.0 or more cancels the damage entirely")
            .add()
            .build();

    private double reduction = 1.0;

    @Override
    public boolean execute(@Nonnull ActionContext context) {
        var damage = context.getDamage();
        if (damage == null) return false;

        float landed = damage.getAmount();
        if (landed <= 0f) return false;

        double remaining = landed * (1.0 - reduction * context.getValue());
        damage.setAmount((float) Math.max(remaining, 0.0));
        return true;
    }
}
