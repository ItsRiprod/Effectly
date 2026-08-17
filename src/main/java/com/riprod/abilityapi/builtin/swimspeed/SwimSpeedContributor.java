package com.riprod.abilityapi.builtin.swimspeed;

import com.riprod.abilityapi.ability.AbilityValue;
import com.riprod.abilityapi.core.AbilityContext;
import com.riprod.abilityapi.core.stat.StatAccumulator;
import com.riprod.abilityapi.core.stat.StatContributor;
import com.riprod.abilityapi.systems.AbilityConditionService;
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
