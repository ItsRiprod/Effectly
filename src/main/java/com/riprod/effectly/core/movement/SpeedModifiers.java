package com.riprod.effectly.core.movement;

import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.riprod.effectly.core.abilities.registry.AbilityContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SpeedModifiers {

    private SpeedModifiers() {}

    public static void put(@Nonnull AbilityContext context, @Nonnull String key, float factor) {
        SpeedModifierComponent store = context.getComponents()
                .getComponent(context.getRef(), SpeedModifierComponent.getComponentType());
        if (store == null) store = new SpeedModifierComponent();
        store.put(key, factor);
        context.getComponents().putComponent(
                context.getRef(), SpeedModifierComponent.getComponentType(), store);
        write(context, store.product());
    }

    public static void remove(@Nonnull AbilityContext context, @Nonnull String key) {
        SpeedModifierComponent store = context.getComponents()
                .getComponent(context.getRef(), SpeedModifierComponent.getComponentType());
        if (store == null || !store.remove(key)) return;

        write(context, store.product());
        if (store.isEmpty()) {
            context.getComponents().tryRemoveComponent(
                    context.getRef(), SpeedModifierComponent.getComponentType());
        }
    }

    private static void write(@Nonnull AbilityContext context, float multiplier) {
        MovementManager movementManager = movementManager(context);
        if (movementManager == null) return;

        float target = movementManager.getDefaultSettings().baseSpeed * multiplier;
        if (movementManager.getSettings().baseSpeed == target) return;
        movementManager.getSettings().baseSpeed = target;
        movementManager.update(context.getPlayerRef().getPacketHandler());
    }

    @Nullable
    private static MovementManager movementManager(@Nonnull AbilityContext context) {
        return context.getComponents()
                .getComponent(context.getRef(), EntityModule.get().getMovementManagerComponentType());
    }
}
