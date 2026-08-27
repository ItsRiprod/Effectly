package com.riprod.effectly.builtin.effects.movespeed;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.registry.AbilityContext;
import com.riprod.effectly.core.effects.components.AbilityValue;
import com.riprod.effectly.core.effects.registry.EffectHandler;
import com.riprod.effectly.core.movement.SpeedModifierComponent;
import com.riprod.effectly.core.movement.SpeedModifiers;
import com.riprod.effectly.core.utils.AbilityConditionUtils;

import javax.annotation.Nonnull;

public final class MoveSpeedHandler implements EffectHandler {

    public static final String ID = "move_speed";

    private static final String MODIFIER_KEY = "Effectly:move_speed";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<MoveSpeedConfig> getConfigBinding() {
        return ConfigBinding.of(MoveSpeedConfig.class, MoveSpeedConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        MoveSpeedComponent.register(registry);
        SpeedModifierComponent.register(registry);
        registry.registerSystem(new MoveSpeedSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        MoveSpeedComponent component = context.getComponents()
                .getComponent(context.getRef(), MoveSpeedComponent.getComponentType());
        if (component == null) component = new MoveSpeedComponent();
        component.bind(abilityId);
        context.getComponents().putComponent(
                context.getRef(), MoveSpeedComponent.getComponentType(), component);
        apply(context, component);
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().tryRemoveComponent(context.getRef(), MoveSpeedComponent.getComponentType());
        SpeedModifiers.remove(context, MODIFIER_KEY);
    }

    static void apply(@Nonnull AbilityContext context, @Nonnull MoveSpeedComponent component) {
        SpeedModifiers.put(context, MODIFIER_KEY, resolveMultiplier(context, component));
    }

    private static float resolveMultiplier(
            @Nonnull AbilityContext context, @Nonnull MoveSpeedComponent component) {
        String abilityId = component.getAbilityId();
        if (abilityId == null) return 1.0f;

        AbilityValue value = AbilityConditionUtils.getActiveAbilityValue(
                context.getRef(), context.getComponents(), context.getWorld(), context.getUuid(), abilityId);
        if (value == null || !value.isPresent() || !(value.getRaw() instanceof Number multiplier)) return 1.0f;

        float factor = multiplier.floatValue();
        return factor > 0f ? factor : 1.0f;
    }
}
