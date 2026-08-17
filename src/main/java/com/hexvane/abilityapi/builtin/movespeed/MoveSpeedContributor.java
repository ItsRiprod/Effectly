package com.hexvane.abilityapi.builtin.movespeed;

import com.hexvane.abilityapi.ability.AbilityValue;
import com.hexvane.abilityapi.core.AbilityContext;
import com.hexvane.abilityapi.core.stat.StatAccumulator;
import com.hexvane.abilityapi.core.stat.StatContributor;
import com.hexvane.abilityapi.systems.AbilityConditionService;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class MoveSpeedContributor implements StatContributor {

    @Nonnull
    @Override
    public ComponentType<EntityStore, ?> componentType() {
        return MoveSpeedComponent.getComponentType();
    }

    @Override
    public void contribute(@Nonnull AbilityContext context, @Nonnull StatAccumulator accumulator) {
        AbilityValue value = AbilityConditionService.getActiveAbilityValue(
                context.getRef(), context.getComponents(), context.getWorld(), context.getUuid(), MoveSpeedHandler.ID);
        if (value == null || !value.isPresent() || !(value.getRaw() instanceof Number multiplier)) return;
        accumulator.multiplyBaseSpeed(multiplier.floatValue());
    }
}
