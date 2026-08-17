package com.riprod.abilityapi.core.equipment;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EquipmentAbilityComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, EquipmentAbilityComponent> COMPONENT_TYPE;

    private Map<String, Double> applied = new LinkedHashMap<>();

    @Nonnull
    public static ComponentType<EntityStore, EquipmentAbilityComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(
                EquipmentAbilityComponent.class, EquipmentAbilityComponent::new);
    }

    @Nonnull
    public Map<String, Double> getApplied() {
        return applied;
    }

    public void setApplied(@Nonnull Map<String, Double> applied) {
        this.applied = applied;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        EquipmentAbilityComponent copy = new EquipmentAbilityComponent();
        copy.applied = new LinkedHashMap<>(applied);
        return copy;
    }
}
