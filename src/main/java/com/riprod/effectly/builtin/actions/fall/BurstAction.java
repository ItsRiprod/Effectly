package com.riprod.effectly.builtin.actions.fall;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.effectly.builtin.capabilities.MagnitudeCapability;
import com.riprod.effectly.builtin.capabilities.PositionCapability;
import com.riprod.effectly.builtin.capabilities.SelfCapability;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

public final class BurstAction extends Action {

    @Nonnull
    public static final String ID = "Burst";

    private static final Set<CapabilityType<?>> REQUIRED =
            Set.of(SelfCapability.TYPE, PositionCapability.TYPE, MagnitudeCapability.TYPE);

    @Nonnull
    public static final BuilderCodec<@NotNull BurstAction> CODEC = BuilderCodec
            .builder(BurstAction.class, BurstAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("Radius", Codec.DOUBLE),
                    (action, v) -> action.radius = v,
                    action -> action.radius)
            .documentation("Radius in blocks around the landing point")
            .add()
            .append(new KeyedCodec<>("Multiplier", Codec.DOUBLE),
                    (action, v) -> action.multiplier = v,
                    action -> action.multiplier)
            .documentation("Multiplier for the burst damage")
            .add()
            .append(new KeyedCodec<>("DamageCause", Codec.STRING),
                    (action, v) -> action.damageCause = v,
                    action -> action.damageCause)
            .documentation("DamageCause asset id the burst damage is dealt as")
            .addValidator(Validators.nonEmptyString())
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    private double radius = 4.0;
    private double multiplier = 1.0;
    private String damageCause = "Effectly_Burst";

    @Nonnull
    @Override
    public Set<CapabilityType<?>> requiredCapabilities() {
        return REQUIRED;
    }

    @Override
    public boolean execute(@Nonnull ActionContext context, double value) {
        if (radius <= 0) return false;

        DamageCause cause = DamageCause.getAssetMap().getAsset(damageCause);
        if (cause == null) return false;

        float amount = (float) (value * context.get(MagnitudeCapability.TYPE).asNumber() * multiplier);
        if (amount <= 0f) return false;

        var store = context.getEntityStore();
        var self = context.get(SelfCapability.TYPE).getEntity();
        List<Ref<EntityStore>> targets = List.copyOf(TargetUtil.getAllEntitiesInSphere(
                context.get(PositionCapability.TYPE).getPosition(), radius, store));

        boolean hit = false;
        for (Ref<EntityStore> target : targets) {
            if (target == null || !target.isValid() || target.equals(self)) continue;
            if (store.getComponent(target, EntityStatMap.getComponentType()) == null) continue;

            DamageSystems.executeDamage(target, store,
                    new Damage(new Damage.EntitySource(self), cause, amount));
            hit = true;
        }
        return hit;
    }
}
