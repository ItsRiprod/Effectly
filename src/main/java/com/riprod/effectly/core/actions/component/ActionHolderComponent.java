package com.riprod.effectly.core.actions.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

public final class ActionHolderComponent implements Component<EntityStore> {

    public static final String COMPONENT_ID = "Effectly:ActionHolder";

    @Nonnull
    public static final BuilderCodec<@NotNull ActionHolderComponent> CODEC = BuilderCodec
            .builder(ActionHolderComponent.class, ActionHolderComponent::new)
            .append(new KeyedCodec<>("Cooldowns",
                            new MapCodec<>(Codec.DOUBLE, LinkedHashMap::new, false)),
                    (holder, v) -> {
                        holder.cooldowns.clear();
                        if (v != null) holder.cooldowns.putAll(v);
                    },
                    holder -> holder.cooldowns.isEmpty() ? null : holder.cooldowns)
            .documentation("Seconds left on each action cooldown, keyed by ability id, trigger id "
                    + "and action index")
            .add()
            .build();

    private static ComponentType<EntityStore, ActionHolderComponent> componentType;

    private final Object2DoubleOpenHashMap<String> cooldowns = new Object2DoubleOpenHashMap<>();

    @Nonnull
    public static ComponentType<EntityStore, ActionHolderComponent> getComponentType() {
        return componentType;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        if (componentType == null) {
            componentType = registry.registerComponent(
                    ActionHolderComponent.class, COMPONENT_ID, CODEC);
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
        ObjectIterator<Object2DoubleMap.Entry<String>> iterator =
                cooldowns.object2DoubleEntrySet().iterator();
        while (iterator.hasNext()) {
            Object2DoubleMap.Entry<String> entry = iterator.next();
            double remaining = entry.getDoubleValue() - dt;
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
