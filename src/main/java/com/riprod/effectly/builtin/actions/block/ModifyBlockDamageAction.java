package com.riprod.effectly.builtin.actions.block;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.effectly.builtin.capabilities.BlockDamageCapability;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import java.util.Set;
import javax.annotation.Nonnull;

public final class ModifyBlockDamageAction extends Action {

    @Nonnull
    public static final String ID = "Modify_Block_Damage";

    private static final Set<CapabilityType<?>> REQUIRED = Set.of(BlockDamageCapability.TYPE);

    @Nonnull
    public static final BuilderCodec<ModifyBlockDamageAction> CODEC = BuilderCodec
            .builder(ModifyBlockDamageAction.class, ModifyBlockDamageAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("Base", Codec.DOUBLE),
                    (action, v) -> action.base = v,
                    action -> action.base)
            .documentation("Constant term of the multiplier. Block damage is scaled by "
                    + "Base + Factor * value")
            .add()
            .append(new KeyedCodec<>("Factor", Codec.DOUBLE),
                    (action, v) -> action.factor = v,
                    action -> action.factor)
            .documentation("How much the granted value contributes to the multiplier")
            .add()
            .build();

    private double base = 1.0;
    private double factor = 0.0;

    @Nonnull
    @Override
    public Set<CapabilityType<?>> requiredCapabilities() {
        return REQUIRED;
    }

    @Override
    public boolean execute(@Nonnull ActionContext context, double value) {
        BlockDamageCapability capability = context.get(BlockDamageCapability.TYPE);

        float current = capability.getDamage();
        if (current <= 0f) return false;

        double multiplier = base + factor * value;
        if (multiplier == 1.0) return false;

        capability.setDamage((float) Math.max(current * multiplier, 0.0));
        return true;
    }
}
