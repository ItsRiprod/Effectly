package com.riprod.effectly.builtin.triggers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.ResolvedAbilityComponent;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.ActionDispatch;
import com.riprod.effectly.core.actions.component.ActionHolderComponent;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class OnDamageFilterProducerSystem extends VictimDamageProducerSystem {

    public static final String TRIGGER = "OnDamageFilter";

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        DamageModule module = DamageModule.get();
        return module != null ? module.getFilterDamageGroup() : null;
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        DamageModule module = DamageModule.get();
        SystemGroup<EntityStore> gather = module != null ? module.getGatherDamageGroup() : null;
        if (gather == null) return Set.of();
        return Set.of(new SystemGroupDependency<>(Order.AFTER, gather));
    }

    @Override
    protected boolean wantsAny(@Nonnull ResolvedAbilityComponent resolved) {
        return resolved.hasTrigger(TRIGGER);
    }

    @Override
    protected void dispatch(
            @Nonnull Ref<EntityStore> holder,
            @Nonnull ActionHolderComponent holderComponent,
            @Nonnull ActionContext context,
            @Nullable Ref<EntityStore> attacker,
            @Nonnull Damage damage) {
        ActionDispatch.fire(TRIGGER, holder, holderComponent, context, attacker);
    }
}
