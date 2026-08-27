package com.riprod.effectly.core.equipment;

import java.util.List;
import javax.annotation.Nonnull;

import com.riprod.effectly.core.conditions.AbilityConditionSpec;

public record EquipmentGrant(double value, @Nonnull List<AbilityConditionSpec> conditions) {

    public EquipmentGrant {
        conditions = List.copyOf(conditions);
    }
}
