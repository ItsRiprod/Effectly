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
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;

import java.util.List;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

public final class BurstAction extends Action {

    @Nonnull
    public static final String ID = "Burst";

    @Nonnull
    public static final BuilderCodec<@NotNull BurstAction> CODEC = BuilderCodec
            .builder(BurstAction.class, BurstAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("Radius", Codec.DOUBLE),
                    (action, v) -> action.radius = v,
                    action -> action.radius)
            .documentation("Radius in blocks around the landing point")
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
    private String damageCause = "Effectly_Burst";

    @Override
    public boolean execute(@Nonnull ActionContext context) {
        if (radius <= 0) return false;

        DamageCause cause = DamageCause.getAssetMap().getAsset(damageCause);
        if (cause == null) return false;

        float amount = (float) (context.getValue() * context.getMagnitude());
        if (amount <= 0f) return false;

        List<Ref<EntityStore>> targets = List.copyOf(
                TargetUtil.getAllEntitiesInSphere(context.getPosition(), radius, context.getStore()));

        boolean hit = false;
        for (Ref<EntityStore> target : targets) {
            if (target == null || !target.isValid() || target.equals(context.getHolderRef())) continue;
            if (context.getStore().getComponent(target, EntityStatMap.getComponentType()) == null) continue;

            DamageSystems.executeDamage(target, context.getCommandBuffer(),
                    new Damage(new Damage.EntitySource(context.getHolderRef()), cause, amount));
            hit = true;
        }
        return hit;
    }
}
