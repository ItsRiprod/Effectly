package com.hexvane.abilityapi.core.stat;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;

public final class StatPacingComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, StatPacingComponent> COMPONENT_TYPE;

    private float accumulator;
    private boolean lastSwimming;
    private boolean applyNow = true;
    private Set<Integer> appliedStats = Set.of();

    @Nonnull
    public Set<Integer> getAppliedStats() {
        return appliedStats;
    }

    public void setAppliedStats(@Nonnull Set<Integer> appliedStats) {
        this.appliedStats = appliedStats;
    }

    @Nonnull
    public static ComponentType<EntityStore, StatPacingComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(StatPacingComponent.class, StatPacingComponent::new);
    }

    public void requestApply() {
        applyNow = true;
    }

    public boolean due(float dt, float interval, boolean swimming) {
        boolean swimChanged = lastSwimming != swimming;
        lastSwimming = swimming;
        if (applyNow || swimChanged) {
            applyNow = false;
            accumulator = 0f;
            return true;
        }
        accumulator += dt;
        if (accumulator < interval) return false;
        accumulator -= interval;
        return true;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        StatPacingComponent copy = new StatPacingComponent();
        copy.accumulator = accumulator;
        copy.lastSwimming = lastSwimming;
        copy.applyNow = applyNow;
        copy.appliedStats = Set.copyOf(appliedStats);
        return copy;
    }
}
