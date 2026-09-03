package com.riprod.effectly.core.actions.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nonnull;

public final class ActionHolderComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, ActionHolderComponent> componentType;

    private final Map<String, Double> cooldowns = new HashMap<>();

    @Nonnull
    public static ComponentType<EntityStore, ActionHolderComponent> getComponentType() {
        return componentType;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        if (componentType == null) {
            componentType = registry.registerComponent(
                    ActionHolderComponent.class, ActionHolderComponent::new);
        }
    }

    public boolean isOnCooldown(@Nonnull String key) {
        return cooldowns.containsKey(key);
    }

    public void startCooldown(@Nonnull String key, double seconds) {
        cooldowns.put(key, seconds);
    }

    public void tickCooldowns(float dt) {
        if (cooldowns.isEmpty()) return;
        Iterator<Map.Entry<String, Double>> iterator = cooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            double remaining = entry.getValue() - dt;
            if (remaining <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remaining);
            }
        }
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        ActionHolderComponent copy = new ActionHolderComponent();
        copy.cooldowns.putAll(this.cooldowns);
        return copy;
    }
}
