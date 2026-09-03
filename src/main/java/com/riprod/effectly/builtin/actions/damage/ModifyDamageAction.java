package com.riprod.effectly.builtin.actions.damage;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.riprod.effectly.builtin.capabilities.DamageCapability;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ModifyDamageAction extends Action {

    @Nonnull
    public static final String ID = "Modify_Damage";

    private static final Set<CapabilityType<?>> REQUIRED = Set.of(DamageCapability.TYPE);

    private static final int MAX_CHAIN_DEPTH = 8;

    @Nonnull
    public static final BuilderCodec<ModifyDamageAction> CODEC = BuilderCodec
            .builder(ModifyDamageAction.class, ModifyDamageAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("Base", Codec.DOUBLE),
                    (action, v) -> action.base = v,
                    action -> action.base)
            .documentation("Constant term of the multiplier. The damage is scaled by "
                    + "Base + Factor * value, so Base 1 Factor -1 is a resistance, Base 1 Factor 1 "
                    + "is an amplifier, and Base 0 Factor 1 uses the value as the multiplier")
            .add()
            .append(new KeyedCodec<>("Factor", Codec.DOUBLE),
                    (action, v) -> action.factor = v,
                    action -> action.factor)
            .documentation("How much the granted value contributes to the multiplier. Zero ignores "
                    + "the value entirely")
            .add()
            .append(new KeyedCodec<>("DamageCause", Codec.STRING),
                    (action, v) -> action.damageCause = v,
                    action -> action.damageCause)
            .documentation("Only modify damage of this cause. Omit to modify every cause")
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("MatchInherited", Codec.BOOLEAN),
                    (action, v) -> action.matchInherited = v,
                    action -> action.matchInherited)
            .documentation("Also match causes that inherit from DamageCause, so a cause of "
                    + "Elemental covers Fire. Ignored when DamageCause is omitted")
            .add()
            .build();

    private double base = 1.0;
    private double factor = 0.0;
    private String damageCause;
    private boolean matchInherited = true;

    @Nonnull
    @Override
    public Set<CapabilityType<?>> requiredCapabilities() {
        return REQUIRED;
    }

    @Override
    public boolean execute(@Nonnull ActionContext context, double value) {
        DamageCapability capability = context.get(DamageCapability.TYPE);
        if (!matches(capability.getCause())) return false;

        var damage = capability.getDamage();
        float current = damage.getAmount();
        if (current <= 0f) return false;

        double multiplier = base + factor * value;
        if (multiplier == 1.0) return false;

        damage.setAmount((float) Math.max(current * multiplier, 0.0));
        return true;
    }

    private boolean matches(@Nullable DamageCause cause) {
        if (damageCause == null || damageCause.isEmpty()) return true;
        if (cause == null) return false;

        DamageCause current = cause;
        for (int depth = 0; current != null && depth < MAX_CHAIN_DEPTH; depth++) {
            String id = current.getId();
            if (id != null && id.equalsIgnoreCase(damageCause)) return true;
            if (!matchInherited) return false;

            String inherits = current.getInherits();
            if (inherits == null || inherits.isBlank()) return false;
            current = DamageCause.getAssetMap().getAsset(inherits);
        }
        return false;
    }
}
