package com.riprod.effectly.builtin.triggers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.builtin.capabilities.DamageCapability;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.ActionDispatch;
import com.riprod.effectly.core.actions.component.ActionHolderComponent;
import com.riprod.effectly.core.damage.DamageModifierPipelineDependencies;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class OnDamagedProducerSystem extends VictimDamageProducerSystem {

    public static final String TRIGGER = "OnDamaged";

    public static final String TRIGGER_ATTACKED = "OnAttacked";

    public static final String TRIGGER_FALL = "OnFall";

    private static final String FALL_CAUSE_ID = "Fall";

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return DamageModifierPipelineDependencies.afterFilterBeforeApplyDamage();
    }

    @Override
    protected boolean wantsAny(@Nonnull ResolvedAbilityComponent resolved) {
        return resolved.hasTrigger(TRIGGER)
                || resolved.hasTrigger(TRIGGER_ATTACKED)
                || resolved.hasTrigger(TRIGGER_FALL);
    }

    @Override
    protected void dispatch(
            @Nonnull Ref<EntityStore> holder,
            @Nonnull ActionHolderComponent holderComponent,
            @Nonnull ActionContext context,
            @Nullable Ref<EntityStore> attacker,
            @Nonnull Damage damage) {
        ActionDispatch.fire(TRIGGER, holder, holderComponent, context, attacker);

        if (attacker != null) {
            ActionDispatch.fire(TRIGGER_ATTACKED, holder, holderComponent, context, attacker);
        }

        DamageCause cause = context.get(DamageCapability.TYPE).getCause();
        if (cause != null && FALL_CAUSE_ID.equals(cause.getId())) {
            ActionDispatch.fire(TRIGGER_FALL, holder, holderComponent, context, attacker);
        }
    }
}
