package com.riprod.effectly.builtin.actions.damage;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.riprod.effectly.builtin.capabilities.DamageCapability;
import com.riprod.effectly.builtin.capabilities.SelfCapability;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import java.util.Set;
import javax.annotation.Nonnull;

public final class SecondChanceAction extends Action {

    @Nonnull
    public static final String ID = "Second_Chance";

    private static final Set<CapabilityType<?>> REQUIRED =
            Set.of(SelfCapability.TYPE, DamageCapability.TYPE);

    @Nonnull
    public static final BuilderCodec<SecondChanceAction> CODEC = BuilderCodec
            .builder(SecondChanceAction.class, SecondChanceAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("RestorePercent", Codec.DOUBLE),
                    (action, v) -> action.restorePercent = v,
                    action -> action.restorePercent)
            .documentation("Fraction of max health the holder is left on after surviving what "
                    + "would have been a lethal hit")
            .add()
            .build();

    private double restorePercent = 0.2;

    @Nonnull
    @Override
    public Set<CapabilityType<?>> requiredCapabilities() {
        return REQUIRED;
    }

    @Override
    public boolean execute(@Nonnull ActionContext context, double value) {
        var damage = context.get(DamageCapability.TYPE).getDamage();
        var self = context.get(SelfCapability.TYPE).getEntity();

        EntityStatMap statMap = context.getEntityStore().getComponent(
                self, EntityStatMap.getComponentType());
        if (statMap == null) return false;

        int healthIndex = DefaultEntityStatTypes.getHealth();
        if (healthIndex < 0 || healthIndex >= statMap.size()) return false;

        EntityStatValue healthStat = statMap.get(healthIndex);
        if (healthStat == null) return false;

        float currentHealth = healthStat.get();
        float maxHealth = healthStat.getMax();
        if (maxHealth <= 0f) return false;

        float lethalThreshold = currentHealth - healthStat.getMin();
        if (damage.getAmount() < lethalThreshold) return false;

        float targetHealth = (float) (restorePercent * maxHealth);
        float newDamage = currentHealth - targetHealth;

        if (newDamage <= 0f) {
            damage.setAmount(0f);
            statMap.addStatValue(healthIndex, targetHealth - currentHealth);
        } else {
            damage.setAmount(newDamage);
        }
        return true;
    }
}
