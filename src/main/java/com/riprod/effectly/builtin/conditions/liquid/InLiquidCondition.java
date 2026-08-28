package com.riprod.effectly.builtin.conditions.liquid;

import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;
import com.riprod.effectly.core.conditions.ConditionContext;
import com.riprod.effectly.core.conditions.registry.ConditionAsset;
import com.riprod.effectly.core.conditions.registry.ConditionHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class InLiquidCondition implements ConditionHandler {

    public static final String ID = "in_liquid";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<InLiquidConfig> getConfigBinding() {
        return ConfigBinding.of(InLiquidConfig.class, InLiquidConfig.CODEC);
    }

    @Override
    public boolean test(
            @Nonnull ConditionContext context,
            @Nonnull ConditionAsset asset,
            @Nonnull AbilityConditionSpec spec) {
        MovementStatesComponent component = context.getComponents()
                .getComponent(context.getRef(), MovementStatesComponent.getComponentType());
        if (component == null) return false;

        MovementStates states = component.getMovementStates();
        if (states == null) return false;

        InLiquidConfig config = asset.configOrDefault(InLiquidConfig.class, InLiquidConfig.DEFAULTS);
        return config.getState() == InLiquidConfig.State.SWIMMING ? states.swimming : states.inFluid;
    }

    @Nullable
    @Override
    public Parsed parse(@Nonnull ConditionAsset asset, @Nonnull String[] remaining) {
        return new Parsed(new AbilityConditionSpec(asset.getId()), 0);
    }
}
