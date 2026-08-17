package com.riprod.abilityapi.builtin.flight;

import com.riprod.abilityapi.core.AbilityContext;
import com.riprod.abilityapi.core.AbilityEntry;
import com.riprod.abilityapi.core.AbilityHandler;
import com.riprod.abilityapi.systems.AbilityConditionService;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class FlightHandler implements AbilityHandler {

    public static final String ID = "creative_flight";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<FlightConfig> getConfigBinding() {
        return ConfigBinding.of(FlightConfig.class, FlightConfig.CODEC);
    }

    @Override
    public void install(@Nonnull ComponentRegistryProxy<EntityStore> registry) {
        FlightComponent.register(registry);
        registry.registerSystem(new FlightReassertSystem());
    }

    @Override
    public void grant(@Nonnull AbilityContext context, @Nonnull String abilityId, @Nonnull AbilityEntry entry) {
        FlightComponent component = context.getComponents().getComponent(context.getRef(), FlightComponent.getComponentType());
        if (component == null) component = new FlightComponent();
        component.bind(abilityId);
        context.getComponents().putComponent(context.getRef(), FlightComponent.getComponentType(), component);
        applyCanFly(context, isActive(context));
    }

    @Override
    public void revoke(@Nonnull AbilityContext context, @Nonnull String abilityId) {
        context.getComponents().tryRemoveComponent(context.getRef(), FlightComponent.getComponentType());
        applyCanFly(context, false);
    }

    static boolean isActive(@Nonnull AbilityContext context) {
        return AbilityConditionService.isAbilityActive(
                context.getRef(), context.getComponents(), context.getWorld(), context.getUuid(), ID);
    }

    static void applyCanFly(@Nonnull AbilityContext context, boolean enabled) {
        MovementManager movementManager = context.getComponents()
                .getComponent(context.getRef(), EntityModule.get().getMovementManagerComponentType());
        if (movementManager == null) return;

        Player player = context.getComponents().getComponent(context.getRef(), Player.getComponentType());
        boolean creative = player != null && player.getGameMode() == GameMode.Creative;
        boolean target = enabled || creative;

        if (movementManager.getSettings().canFly == target) return;
        movementManager.getSettings().canFly = target;
        movementManager.update(context.getPlayerRef().getPacketHandler());
    }
}
