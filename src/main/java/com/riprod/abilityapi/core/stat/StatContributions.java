package com.riprod.abilityapi.core.stat;

import com.riprod.abilityapi.core.AbilityContext;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public final class StatContributions {

    private static final List<StatContributor> CONTRIBUTORS = new ArrayList<>();
    private static boolean queryMaterialised;

    private StatContributions() {}

    public static void reset() {
        CONTRIBUTORS.clear();
        queryMaterialised = false;
    }

    public static void register(@Nonnull StatContributor contributor) {
        if (queryMaterialised) {
            throw new IllegalStateException(
                    "stat contributor registered after AbilityStatSystem was built: " + contributor.getClass().getName());
        }
        CONTRIBUTORS.add(contributor);
    }

    @Nonnull
    public static List<StatContributor> all() {
        return Collections.unmodifiableList(CONTRIBUTORS);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static Query<EntityStore> query() {
        queryMaterialised = true;
        if (CONTRIBUTORS.isEmpty()) return StatPacingComponent.getComponentType();
        Query<EntityStore>[] queries = new Query[CONTRIBUTORS.size()];
        for (int i = 0; i < CONTRIBUTORS.size(); i++) {
            queries[i] = CONTRIBUTORS.get(i).componentType();
        }
        return Query.or(queries);
    }

    public static boolean holdsAny(@Nonnull AbilityContext context) {
        for (StatContributor contributor : CONTRIBUTORS) {
            if (context.getComponents().getComponent(context.getRef(), contributor.componentType()) != null) {
                return true;
            }
        }
        return false;
    }

    public static void onGrant(@Nonnull AbilityContext context) {
        StatPacingComponent pacing = context.getComponents()
                .getComponent(context.getRef(), StatPacingComponent.getComponentType());
        if (pacing == null) {
            context.getComponents().putComponent(
                    context.getRef(), StatPacingComponent.getComponentType(), new StatPacingComponent());
            return;
        }
        pacing.requestApply();
    }

    public static void onRevoke(@Nonnull AbilityContext context) {
        AbilityStatApplier.apply(context);
        if (holdsAny(context)) {
            StatPacingComponent pacing = context.getComponents()
                    .getComponent(context.getRef(), StatPacingComponent.getComponentType());
            if (pacing != null) pacing.requestApply();
            return;
        }
        context.getComponents().removeComponent(context.getRef(), StatPacingComponent.getComponentType());
    }

    @Nonnull
    public static StatAccumulator accumulate(@Nonnull AbilityContext context) {
        StatAccumulator accumulator = new StatAccumulator();
        for (StatContributor contributor : CONTRIBUTORS) {
            ComponentType<EntityStore, ?> type = contributor.componentType();
            if (context.getComponents().getComponent(context.getRef(), type) == null) continue;
            contributor.contribute(context, accumulator);
        }
        return accumulator;
    }
}
