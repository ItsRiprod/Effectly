package com.riprod.effectly.builtin.actions.fall;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;

import javax.annotation.Nonnull;

import org.joml.Vector3d;

public final class BounceAction extends Action {

    @Nonnull
    public static final String ID = "Bounce";

    @Nonnull
    public static final BuilderCodec<BounceAction> CODEC = BuilderCodec
            .builder(BounceAction.class, BounceAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("Restitution", Codec.DOUBLE),
                    (action, v) -> action.restitution = v,
                    action -> action.restitution)
            .documentation("Upward launch strength. The impulse is Restitution * value * "
                    + "sqrt(fall distance)")
            .add()
            .build();

    private double restitution = 4.0;

    @Override
    public boolean execute(@Nonnull ActionContext context) {
        Velocity velocity = context.getStore().getComponent(
                context.getHolderRef(), Velocity.getComponentType());
        if (velocity == null) return false;

        double vy = restitution * context.getValue() * Math.sqrt(context.getMagnitude());
        if (vy <= 0) return false;

        velocity.addInstruction(new Vector3d(0, vy, 0), null, ChangeVelocityType.Add);
        return true;
    }
}
