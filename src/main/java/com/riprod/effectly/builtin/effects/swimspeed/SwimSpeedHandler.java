package com.riprod.effectly.builtin.effects.swimspeed;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.registry.AbilityContext;
import com.riprod.effectly.core.effects.components.AbilityValue;
import com.riprod.effectly.core.effects.registry.EffectHandler;
import com.riprod.effectly.core.movement.SpeedModifierComponent;
import com.riprod.effectly.core.movement.SpeedModifiers;
import com.riprod.effectly.core.utils.AbilityConditionUtils;

import javax.annotation.Nonnull;

public final class SwimSpeedHandler implements EffectHandler {

    public static final String ID = "swim_speed";

    private static final String MODIFIER_KEY = "Effectly:swim_speed";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<SwimSpeedConfig> getConfigBinding() {
        return ConfigBinding.of(SwimSpeedConfig.class, SwimSpeedConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        SwimSpeedComponent.register(registry);
        SpeedModifierComponent.register(registry);
        registry.registerSystem(new SwimSpeedSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        SwimSpeedComponent component = context.getComponents()
                .getComponent(context.getRef(), SwimSpeedComponent.getComponentType());
        if (component == null) component = new SwimSpeedComponent();
        component.bind(abilityId);
        context.getComponents().putComponent(
                context.getRef(), SwimSpeedComponent.getComponentType(), component);
        apply(context, component);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().tryRemoveComponent(context.getRef(), SwimSpeedComponent.getComponentType());
        SpeedModifiers.remove(context, MODIFIER_KEY);
    }

    static void apply(@Nonnull AbilityContext context, @Nonnull SwimSpeedComponent component) {
        SpeedModifiers.put(context, MODIFIER_KEY, resolveMultiplier(context, component));
    }

    static boolean isSwimming(@Nonnull AbilityContext context) {
        MovementStatesComponent movementStates = context.getComponents()
                .getComponent(context.getRef(), MovementStatesComponent.getComponentType());
        return movementStates != null
                && movementStates.getMovementStates() != null
                && movementStates.getMovementStates().swimming;
    }

    private static float resolveMultiplier(
            @Nonnull AbilityContext context, @Nonnull SwimSpeedComponent component) {
        String abilityId = component.getAbilityId();
        if (abilityId == null || !isSwimming(context)) return 1.0f;

        AbilityValue value = AbilityConditionUtils.getActiveAbilityValue(
                context.getRef(), context.getComponents(), context.getWorld(), context.getUuid(), abilityId);
        if (value == null || !value.isPresent() || !(value.getRaw() instanceof Number multiplier)) return 1.0f;

        float factor = multiplier.floatValue();
        return factor > 0f ? factor : 1.0f;
    }
}
