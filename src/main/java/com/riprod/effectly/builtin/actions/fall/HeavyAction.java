package com.riprod.effectly.builtin.actions.fall;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.effectly.builtin.capabilities.DamageCapability;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import java.util.Set;
import javax.annotation.Nonnull;

public final class HeavyAction extends Action {

    @Nonnull
    public static final String ID = "Heavy";

    private static final Set<CapabilityType<?>> REQUIRED = Set.of(DamageCapability.TYPE);

    @Nonnull
    public static final BuilderCodec<HeavyAction> CODEC = BuilderCodec
            .builder(HeavyAction.class, HeavyAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("Reduction", Codec.DOUBLE),
                    (action, v) -> action.reduction = v,
                    action -> action.reduction)
            .documentation("Fraction of the damage removed at value 1.0. Reduction * value of "
                    + "1.0 or more cancels the damage entirely")
            .add()
            .build();

    private double reduction = 1.0;

    @Nonnull
    @Override
    public Set<CapabilityType<?>> requiredCapabilities() {
        return REQUIRED;
    }

    @Override
    public boolean execute(@Nonnull ActionContext context, double value) {
        var damage = context.get(DamageCapability.TYPE).getDamage();

        float landed = damage.getAmount();
        if (landed <= 0f) return false;

        double remaining = landed * (1.0 - reduction * value);
        damage.setAmount((float) Math.max(remaining, 0.0));
        return true;
    }
}
