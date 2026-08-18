package com.riprod.effectly.core.equipment;

import com.riprod.effectly.ability.AbilityConditionSpec;
import java.util.List;
import javax.annotation.Nonnull;

public record EquipmentGrant(double value, @Nonnull List<AbilityConditionSpec> conditions) {

    public EquipmentGrant {
        conditions = List.copyOf(conditions);
    }
}
