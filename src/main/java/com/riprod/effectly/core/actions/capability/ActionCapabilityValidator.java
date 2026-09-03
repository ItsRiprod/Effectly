package com.riprod.effectly.core.actions.capability;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.trigger.Trigger;
import com.riprod.effectly.core.actions.trigger.TriggerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public final class ActionCapabilityValidator implements Validator<Action[]> {

    public static final ActionCapabilityValidator INSTANCE = new ActionCapabilityValidator();

    private ActionCapabilityValidator() {
    }

    @Override
    public void accept(Action[] actions, @Nonnull ValidationResults results) {
        if (actions == null) return;
        for (Action action : actions) {
            if (action == null) continue;
            String triggerId = action.getTrigger();
            if (triggerId == null || triggerId.isEmpty()) continue;

            Trigger trigger = TriggerRegistry.get(triggerId);
            if (trigger == null) continue;

            validateRequired(action, trigger, results);
            validateThreshold(action, trigger, results);
        }
    }

    private static void validateRequired(
            @Nonnull Action action,
            @Nonnull Trigger trigger,
            @Nonnull ValidationResults results) {
        Set<CapabilityType<?>> required = action.requiredCapabilities();
        for (CapabilityType<?> capability : required) {
            if (trigger.provides(capability)) continue;
            results.fail("Action '" + action.getType() + "' on trigger '" + trigger.getId()
                    + "' requires capability '" + capability.getId() + "', which '" + trigger.getId()
                    + "' does not provide. " + satisfyingTriggers(required));
        }
    }

    private static void validateThreshold(
            @Nonnull Action action,
            @Nonnull Trigger trigger,
            @Nonnull ValidationResults results) {
        for (String key : action.getThreshold().keySet()) {
            CapabilityType<?> capability = CapabilityRegistry.byId(key);
            if (capability == null) {
                results.fail("Unknown Threshold capability '" + key + "'. Registered: "
                        + String.join(", ", CapabilityRegistry.ids()));
                continue;
            }
            if (!capability.isNumeric()) {
                results.fail("Threshold capability '" + key + "' is not numeric and cannot gate an action.");
                continue;
            }
            if (!trigger.provides(capability)) {
                results.fail("Action '" + action.getType() + "' on trigger '" + trigger.getId()
                        + "' thresholds on capability '" + key + "', which '" + trigger.getId()
                        + "' does not provide.");
            }
        }
    }

    @Nonnull
    private static String satisfyingTriggers(@Nonnull Set<CapabilityType<?>> required) {
        List<String> candidates = new ArrayList<>();
        for (Trigger candidate : TriggerRegistry.all()) {
            if (candidate.getProvides().containsAll(required)) candidates.add(candidate.getId());
        }
        return candidates.isEmpty()
                ? "No registered trigger provides all of them."
                : "Triggers providing all required capabilities: " + String.join(", ", candidates) + ".";
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Each action must only require capabilities its trigger provides.");
    }
}
