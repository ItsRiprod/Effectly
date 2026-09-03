package com.riprod.effectly.core.actions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.riprod.effectly.core.actions.capability.Capability;
import com.riprod.effectly.core.actions.capability.CapabilityRegistry;
import com.riprod.effectly.core.actions.capability.CapabilityType;
import com.riprod.effectly.core.actions.capability.NumericCapability;
import com.riprod.effectly.core.actions.effects.ActionEffects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Action {

    private static final Gate[] NO_GATES = new Gate[0];

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
            .append(new KeyedCodec<>("Cooldown", Codec.DOUBLE),
                    (action, v) -> action.cooldown = v,
                    action -> action.cooldown)
            .documentation("Seconds before this action can fire again for the same player.")
            .add()
            .append(new KeyedCodec<>("Threshold",
                            new MapCodec<>(Codec.DOUBLE, LinkedHashMap::new, false)),
                    (action, v) -> action.threshold = v == null ? Map.of() : v,
                    action -> action.threshold.isEmpty() ? null : action.threshold)
            .documentation("Minimum values the trigger must supply for this action to fire, keyed by "
                    + "capability id. Each key must name a numeric capability the trigger provides.")
            .add()
            .append(new KeyedCodec<>("Effects", ActionEffects.CODEC),
                    (action, v) -> action.effects = v,
                    action -> action.effects)
            .documentation("Sounds, particles, animation, camera shake and entity effects played on "
                    + "the holder after this action executes.")
            .add()
            .build();

    protected String type;
    protected double cooldown = 0.0;
    protected Map<String, Double> threshold = Map.of();
    protected ActionEffects effects;

    private volatile Gate[] gates;

    @Nullable
    public String getType() {
        return type;
    }

    public double getCooldown() {
        return cooldown;
    }

    @Nonnull
    public Map<String, Double> getThreshold() {
        return threshold;
    }

    @Nullable
    public ActionEffects getEffects() {
        return effects;
    }

    @Nonnull
    public Set<CapabilityType<?>> requiredCapabilities() {
        return Set.of();
    }

    public boolean thresholdMet(@Nonnull ActionContext context) {
        for (Gate gate : gates()) {
            Capability capability = context.find(gate.type);
            if (!(capability instanceof NumericCapability numeric)) return false;
            if (numeric.asNumber() < gate.min) return false;
        }
        return true;
    }

    /**
     * @return true when the action actually did something. Returning false leaves the cooldown
     *         unspent, so an action that finds nothing to act on does not lock itself out.
     */
    public abstract boolean execute(@Nonnull ActionContext context, double value);

    @Nonnull
    private Gate[] gates() {
        Gate[] local = gates;
        if (local != null) return local;
        if (threshold.isEmpty()) {
            local = NO_GATES;
        } else {
            List<Gate> built = new ArrayList<>(threshold.size());
            for (Map.Entry<String, Double> entry : threshold.entrySet()) {
                CapabilityType<?> capability = CapabilityRegistry.byId(entry.getKey());
                if (capability != null) built.add(new Gate(capability, entry.getValue()));
            }
            local = built.toArray(Gate[]::new);
        }
        gates = local;
        return local;
    }

    private record Gate(@Nonnull CapabilityType<?> type, double min) {}
}
