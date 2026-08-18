package com.riprod.effectly.builtin.conditions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.ability.AbilityConditionSpec;
import com.riprod.effectly.core.condition.AbilityCondition;
import com.riprod.effectly.core.condition.AbilityConditionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class HealthCondition implements AbilityCondition {

    private final String id;
    private final boolean useTarget;
    private final boolean below;

    public HealthCondition(@Nonnull String id, boolean useTarget, boolean below) {
        this.id = id;
        this.useTarget = useTarget;
        this.below = below;
    }

    @Nonnull
    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean test(@Nonnull AbilityConditionContext context, @Nonnull AbilityConditionSpec spec) {
        Ref<EntityStore> ref = useTarget ? context.getTargetRef() : context.getRef();
        float percent = HealthPercent.of(ref, context.getComponents());
        if (!HealthPercent.isKnown(percent)) return false;
        return below ? percent < spec.param() : percent >= spec.param();
    }

    @Nullable
    @Override
    public Parsed parse(@Nonnull String[] remaining) {
        if (remaining.length < 1) return null;
        try {
            int percent = Integer.parseInt(remaining[0]);
            if (percent < 0 || percent > 100) return null;
            return new Parsed(new AbilityConditionSpec(id, percent), 1);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public String describe(@Nonnull AbilityConditionSpec spec) {
        return id + "=" + spec.param();
    }

    @Nonnull
    @Override
    public String usage() {
        return id + " <percent 0-100>";
    }

    @Nonnull
    @Override
    public String description() {
        String who = useTarget ? "damage target" : "your";
        return who + " health " + (below ? "below" : "at or above") + " %";
    }
}
