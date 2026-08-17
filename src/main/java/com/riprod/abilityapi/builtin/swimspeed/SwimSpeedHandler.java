package com.riprod.abilityapi.builtin.swimspeed;

import com.riprod.abilityapi.ability.AbilityValue;
import com.riprod.abilityapi.core.AbilityContext;
import com.riprod.abilityapi.core.AbilityEntry;
import com.riprod.abilityapi.core.AbilityHandler;
import com.riprod.abilityapi.core.movement.SpeedModifierComponent;
import com.riprod.abilityapi.core.movement.SpeedModifiers;
import com.riprod.abilityapi.systems.AbilityConditionService;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class SwimSpeedHandler implements AbilityHandler {

    public static final String ID = "swim_speed";

    private static final String MODIFIER_KEY = "AbilityAPI:swim_speed";

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
        if (component == null) {
            component = new SwimSpeedComponent();
            context.getComponents().putComponent(
                    context.getRef(), SwimSpeedComponent.getComponentType(), component);
        }
        component.bind(abilityId);
        apply(context, component);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().removeComponent(context.getRef(), SwimSpeedComponent.getComponentType());
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

        AbilityValue value = AbilityConditionService.getActiveAbilityValue(
                context.getRef(), context.getComponents(), context.getWorld(), context.getUuid(), abilityId);
        if (value == null || !value.isPresent() || !(value.getRaw() instanceof Number multiplier)) return 1.0f;

        float factor = multiplier.floatValue();
        return factor > 0f ? factor : 1.0f;
    }
}
