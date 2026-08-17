package com.hexvane.abilityapi.builtin.oxygen;

import com.hexvane.abilityapi.ability.AbilityValue;
import com.hexvane.abilityapi.config.AbilityApiConfig;
import com.hexvane.abilityapi.core.AbilityContext;
import com.hexvane.abilityapi.core.stat.StatAccumulator;
import com.hexvane.abilityapi.core.stat.StatContributor;
import com.hexvane.abilityapi.systems.AbilityConditionService;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class OxygenContributor implements StatContributor {

    @Nonnull
    @Override
    public ComponentType<EntityStore, ?> componentType() {
        return OxygenComponent.getComponentType();
    }

    @Override
    public void contribute(@Nonnull AbilityContext context, @Nonnull StatAccumulator accumulator) {
        AbilityValue value = AbilityConditionService.getActiveAbilityValue(
                context.getRef(), context.getComponents(), context.getWorld(), context.getUuid(), OxygenHandler.ID);
        if (value == null || !value.isPresent() || value.asNumber() <= 0) return;

        float extra = (float) (value.asNumber() * AbilityApiConfig.get().getOxygenUnitsPerSecond());
        accumulator.addMaxStat(DefaultEntityStatTypes.getOxygen(), extra);
    }
}
