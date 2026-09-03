package com.riprod.effectly.builtin.actions.fall;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.riprod.effectly.builtin.capabilities.MagnitudeCapability;
import com.riprod.effectly.builtin.capabilities.SelfCapability;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import java.util.Set;
import javax.annotation.Nonnull;

import org.joml.Vector3d;

public final class BounceAction extends Action {

    @Nonnull
    public static final String ID = "Bounce";

    private static final Set<CapabilityType<?>> REQUIRED =
            Set.of(SelfCapability.TYPE, MagnitudeCapability.TYPE);

    @Nonnull
    public static final BuilderCodec<BounceAction> CODEC = BuilderCodec
            .builder(BounceAction.class, BounceAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("Restitution", Codec.DOUBLE),
                    (action, v) -> action.restitution = v,
                    action -> action.restitution)
            .documentation("Upward launch strength. The impulse is Restitution * value * "
                    + "sqrt(magnitude)")
            .add()
            .build();

    private double restitution = 4.0;

    @Nonnull
    @Override
    public Set<CapabilityType<?>> requiredCapabilities() {
        return REQUIRED;
    }

    @Override
    public boolean execute(@Nonnull ActionContext context, double value) {
        var self = context.get(SelfCapability.TYPE).getEntity();
        Velocity velocity = context.getEntityStore().getComponent(self, Velocity.getComponentType());
        if (velocity == null) return false;

        double magnitude = context.get(MagnitudeCapability.TYPE).asNumber();
        double vy = restitution * value * Math.sqrt(Math.max(magnitude, 0.0));
        if (vy <= 0) return false;

        velocity.addInstruction(new Vector3d(0, vy, 0), null, ChangeVelocityType.Add);
        return true;
    }
}
