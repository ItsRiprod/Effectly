package com.hexvane.abilityapi.builtin.secondchance;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class SecondChanceComponent implements Component<EntityStore> {

    public static final String COMPONENT_ID = "AbilityAPI:SecondChance";

    @Nonnull
    public static final BuilderCodec<SecondChanceComponent> CODEC = BuilderCodec
            .builder(SecondChanceComponent.class, SecondChanceComponent::new)
            .append(new KeyedCodec<>("CooldownRemaining", Codec.FLOAT),
                    (state, v) -> state.cooldownRemaining = v,
                    state -> state.cooldownRemaining)
            .documentation("Seconds left before second_chance can trigger again")
            .add()
            .build();

    private static ComponentType<EntityStore, SecondChanceComponent> COMPONENT_TYPE;

    private float cooldownRemaining;

    @Nonnull
    public static ComponentType<EntityStore, SecondChanceComponent> getComponentType() {
        return COMPONENT_TYPE;
    }

    public static void register(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        COMPONENT_TYPE = registry.registerComponent(SecondChanceComponent.class, COMPONENT_ID, CODEC);
    }

    public boolean onCooldown() {
        return cooldownRemaining > 0f;
    }

    public void startCooldown(float seconds) {
        cooldownRemaining = seconds;
    }

    public void tickCooldown(float dt) {
        if (cooldownRemaining > 0f) {
            cooldownRemaining -= dt;
            if (cooldownRemaining < 0f) cooldownRemaining = 0f;
        }
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        SecondChanceComponent copy = new SecondChanceComponent();
        copy.cooldownRemaining = cooldownRemaining;
        return copy;
    }
}
