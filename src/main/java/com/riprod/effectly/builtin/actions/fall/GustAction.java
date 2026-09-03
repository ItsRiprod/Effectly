package com.riprod.effectly.builtin.actions.fall;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.effectly.builtin.capabilities.PositionCapability;
import com.riprod.effectly.builtin.capabilities.SelfCapability;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import org.joml.Vector3d;

public final class GustAction extends Action {

    @Nonnull
    public static final String ID = "Gust";

    private static final Set<CapabilityType<?>> REQUIRED =
            Set.of(SelfCapability.TYPE, PositionCapability.TYPE);

    @Nonnull
    public static final BuilderCodec<GustAction> CODEC = BuilderCodec
            .builder(GustAction.class, GustAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("Radius", Codec.DOUBLE),
                    (action, v) -> action.radius = v,
                    action -> action.radius)
            .documentation("Radius in blocks around the landing point")
            .add()
            .append(new KeyedCodec<>("Force", Codec.DOUBLE),
                    (action, v) -> action.force = v,
                    action -> action.force)
            .documentation("Push strength at the center, scaled by value and falling off "
                    + "linearly to the edge of the radius")
            .add()
            .build();

    private double radius = 4.0;
    private double force = 8.0;

    @Nonnull
    @Override
    public Set<CapabilityType<?>> requiredCapabilities() {
        return REQUIRED;
    }

    @Override
    public boolean execute(@Nonnull ActionContext context, double value) {
        if (radius <= 0) return false;

        var store = context.getEntityStore();
        var self = context.get(SelfCapability.TYPE).getEntity();
        Vector3d center = context.get(PositionCapability.TYPE).getPosition();

        List<Ref<EntityStore>> targets =
                List.copyOf(TargetUtil.getAllEntitiesInSphere(center, radius, store));

        boolean pushed = false;
        for (Ref<EntityStore> target : targets) {
            if (target == null || !target.isValid() || target.equals(self)) continue;

            Velocity velocity = store.getComponent(target, Velocity.getComponentType());
            if (velocity == null) continue;

            TransformComponent transform = store.getComponent(
                    target, TransformComponent.getComponentType());
            if (transform == null) continue;

            Vector3d offset = new Vector3d(transform.getPosition()).sub(center);
            double distance = offset.length();
            if (distance > radius) continue;

            double strength = force * value * (1.0 - distance / radius);
            if (strength <= 0) continue;

            Vector3d direction = distance < 1e-6
                    ? new Vector3d(0, 1, 0)
                    : offset.div(distance);
            direction.y = Math.max(direction.y, 0.25);
            direction.normalize();

            velocity.addInstruction(direction.mul(strength), null, ChangeVelocityType.Add);
            pushed = true;
        }
        return pushed;
    }
}
