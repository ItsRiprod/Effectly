package com.hexvane.abilityapi.builtin.swimspeed;

import com.hexvane.abilityapi.ability.AbilityValue;
import com.hexvane.abilityapi.core.AbilityContext;
import com.hexvane.abilityapi.core.stat.StatAccumulator;
import com.hexvane.abilityapi.core.stat.StatContributor;
import com.hexvane.abilityapi.systems.AbilityConditionService;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class SwimSpeedContributor implements StatContributor {

    @Nonnull
    @Override
    public ComponentType<EntityStore, ?> componentType() {
        return SwimSpeedComponent.getComponentType();
    }

    @Override
    public void contribute(@Nonnull AbilityContext context, @Nonnull StatAccumulator accumulator) {
        AbilityValue value = AbilityConditionService.getActiveAbilityValue(
                context.getRef(), context.getComponents(), context.getWorld(), context.getUuid(), SwimSpeedHandler.ID);
        if (value == null || !value.isPresent() || !(value.getRaw() instanceof Number multiplier)) return;

        MovementStatesComponent movementStates = context.getComponents()
                .getComponent(context.getRef(), MovementStatesComponent.getComponentType());
        boolean swimming = movementStates != null
                && movementStates.getMovementStates() != null
                && movementStates.getMovementStates().swimming;
        if (!swimming) return;

        accumulator.multiplyBaseSpeed(multiplier.floatValue());
    }
}
