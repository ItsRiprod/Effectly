package com.riprod.effectly.core.actions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.effectly.core.actions.trigger.TriggerKeyValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Action {

    @Nonnull
    public static final CodecMapCodec<Action> CODEC = new CodecMapCodec<>("Type", true, false);

    @Nonnull
    public static final BuilderCodec<Action> BASE_CODEC = BuilderCodec
            .abstractBuilder(Action.class)
            .append(new KeyedCodec<>("Type", Codec.STRING),
                    (action, v) -> action.type = v,
                    action -> action.type)
            .addValidatorLate(() -> ActionKeyValidator.INSTANCE.late())
            .documentation("Registered action that executes when the trigger fires. Selects the "
                    + "shape of the rest of the block.")
            .add()
            .append(new KeyedCodec<>("Trigger", Codec.STRING),
                    (action, v) -> action.trigger = v,
                    action -> action.trigger)
            .addValidator(Validators.nonEmptyString())
            .addValidatorLate(() -> TriggerKeyValidator.INSTANCE.late())
            .documentation("Registered trigger this action fires on, e.g. OnFall or OnLand.")
            .add()
            .append(new KeyedCodec<>("Cooldown", Codec.DOUBLE),
                    (action, v) -> action.cooldown = v,
                    action -> action.cooldown)
            .documentation("Seconds before this action can fire again for the same player.")
            .add()
            .append(new KeyedCodec<>("MinFallDistance", Codec.DOUBLE),
                    (action, v) -> action.minFallDistance = v,
                    action -> action.minFallDistance)
            .documentation("Minimum trigger magnitude required to fire. For OnLand this is fall "
                    + "distance in blocks, so trivial hops do not fire the action.")
            .add()
            .build();

    protected String type;
    protected String trigger;
    protected double cooldown = 0.0;
    protected double minFallDistance = 0.0;

    @Nullable
    public String getType() {
        return type;
    }

    @Nullable
    public String getTrigger() {
        return trigger;
    }

    public double getCooldown() {
        return cooldown;
    }

    public double getMinFallDistance() {
        return minFallDistance;
    }

    /**
     * @return true when the action actually did something. Returning false leaves the cooldown
     *         unspent, so an action that finds nothing to act on does not lock itself out.
     */
    public abstract boolean execute(@Nonnull ActionContext context);
}
