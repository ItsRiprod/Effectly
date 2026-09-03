package com.riprod.effectly.builtin.actions.damage;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.riprod.effectly.builtin.capabilities.DamageCapability;
import com.riprod.effectly.builtin.capabilities.OtherEntityCapability;
import com.riprod.effectly.builtin.capabilities.SelfCapability;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import java.util.Set;
import javax.annotation.Nonnull;

public final class ReflectAction extends Action {

    @Nonnull
    public static final String ID = "Reflect";

    private static final Set<CapabilityType<?>> REQUIRED =
            Set.of(SelfCapability.TYPE, OtherEntityCapability.TYPE, DamageCapability.TYPE);

    @Nonnull
    public static final BuilderCodec<ReflectAction> CODEC = BuilderCodec
            .builder(ReflectAction.class, ReflectAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("DamageCause", Codec.STRING),
                    (action, v) -> action.damageCause = v,
                    action -> action.damageCause)
            .documentation("DamageCause asset id the reflected damage is dealt as. Must be its own "
                    + "cause so a hit already carrying it is not reflected again")
            .addValidator(Validators.nonEmptyString())
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    private String damageCause = "Effectly_Thorns";

    @Nonnull
    @Override
    public Set<CapabilityType<?>> requiredCapabilities() {
        return REQUIRED;
    }

    @Override
    public boolean execute(@Nonnull ActionContext context, double value) {
        DamageCapability capability = context.get(DamageCapability.TYPE);

        float landed = capability.getDamage().getAmount();
        if (landed <= 0f) return false;

        DamageCause reflectedCause = DamageCause.getAssetMap().getAsset(damageCause);
        if (reflectedCause == null) return false;

        DamageCause incoming = capability.getCause();
        if (incoming != null && reflectedCause.getId().equalsIgnoreCase(incoming.getId())) return false;

        float reflected = (float) (landed * value);
        if (reflected <= 0f) return false;

        var self = context.get(SelfCapability.TYPE).getEntity();
        var other = context.get(OtherEntityCapability.TYPE).getEntity();

        DamageSystems.executeDamage(other, context.getEntityStore(),
                new Damage(new Damage.EntitySource(self), reflectedCause, reflected));
        return true;
    }
}
