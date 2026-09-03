package com.riprod.effectly.builtin.actions.fall;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.riprod.effectly.builtin.capabilities.DamageCapability;
import com.riprod.effectly.builtin.capabilities.SelfCapability;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import java.util.Set;
import javax.annotation.Nonnull;

public final class VibraniumAction extends Action {

    @Nonnull
    public static final String ID = "Vibranium";

    private static final Set<CapabilityType<?>> REQUIRED =
            Set.of(SelfCapability.TYPE, DamageCapability.TYPE);

    @Nonnull
    public static final BuilderCodec<VibraniumAction> CODEC = BuilderCodec
            .builder(VibraniumAction.class, VibraniumAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("ConversionRate", Codec.DOUBLE),
                    (action, v) -> action.conversionRate = v,
                    action -> action.conversionRate)
            .documentation("Fraction of the absorbed damage returned as healing, scaled by value")
            .add()
            .build();

    private double conversionRate = 0.5;

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

        damage.setAmount(0f);

        var self = context.get(SelfCapability.TYPE).getEntity();
        EntityStatMap statMap = context.getEntityStore().getComponent(
                self, EntityStatMap.getComponentType());
        if (statMap == null) return true;

        float healed = (float) (landed * conversionRate * value);
        if (healed <= 0f) return true;

        statMap.addStatValue(DefaultEntityStatTypes.getHealth(), healed);
        return true;
    }
}
