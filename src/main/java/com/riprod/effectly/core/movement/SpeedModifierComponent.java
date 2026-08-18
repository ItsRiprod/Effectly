package com.riprod.effectly.core.movement;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public final class SpeedModifierComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, SpeedModifierComponent> COMPONENT_TYPE;

    private final Map<String, Float> factors = new LinkedHashMap<>();

    @Nonnull
    public static ComponentType<EntityStore, SpeedModifierComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        if (COMPONENT_TYPE == null) {
            COMPONENT_TYPE = registry.registerComponent(
                    SpeedModifierComponent.class, SpeedModifierComponent::new);
        }
    }

    public void put(@Nonnull String key, float factor) {
        factors.put(key, factor);
    }

    public boolean remove(@Nonnull String key) {
        return factors.remove(key) != null;
    }

    public boolean isEmpty() {
        return factors.isEmpty();
    }

    public float product() {
        float product = 1.0f;
        for (float factor : factors.values()) {
            if (factor > 0f) product *= factor;
        }
        return product;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        SpeedModifierComponent copy = new SpeedModifierComponent();
        copy.factors.putAll(factors);
        return copy;
    }
}
