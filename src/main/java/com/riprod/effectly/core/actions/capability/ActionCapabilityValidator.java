package com.riprod.effectly.core.actions.capability;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionRegistry;
import com.riprod.effectly.core.actions.trigger.Trigger;
import com.riprod.effectly.core.actions.trigger.TriggerRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public final class ActionCapabilityValidator implements Validator<Map<String, Action[]>> {

    public static final ActionCapabilityValidator INSTANCE = new ActionCapabilityValidator();

    private ActionCapabilityValidator() {
    }

    @Override
    public void accept(Map<String, Action[]> triggers, @Nonnull ValidationResults results) {
        if (triggers == null) return;
        for (Map.Entry<String, Action[]> entry : triggers.entrySet()) {
            String triggerId = entry.getKey();
            Trigger trigger = TriggerRegistry.get(triggerId);
            if (trigger == null) {
                results.fail("Unknown trigger '" + triggerId + "'. Registered: "
                        + String.join(", ", TriggerRegistry.ids()));
                continue;
            }
            for (Action action : entry.getValue()) {
                if (action == null) continue;
                validateRequired(action, trigger, results);
                validateThreshold(action, trigger, results);
            }
        }
    }

    private static void validateRequired(
            @Nonnull Action action,
            @Nonnull Trigger trigger,
            @Nonnull ValidationResults results) {
        for (CapabilityType<?> capability : action.requiredCapabilities()) {
            if (trigger.provides(capability)) continue;
            results.fail("Action '" + action.getType() + "' under trigger '" + trigger.getId()
                    + "' requires capability '" + capability.getId() + "', which '" + trigger.getId()
                    + "' does not provide. " + validActionsFor(trigger));
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
                results.fail("Action '" + action.getType() + "' under trigger '" + trigger.getId()
                        + "' thresholds on capability '" + key + "', which '" + trigger.getId()
                        + "' does not provide.");
            }
        }
    }

    @Nonnull
    private static String validActionsFor(@Nonnull Trigger trigger) {
        Set<CapabilityType<?>> provides = trigger.getProvides();
        List<String> candidates = new ArrayList<>();
        for (String id : Action.CODEC.getRegisteredIds()) {
            if (provides.containsAll(ActionRegistry.requiredCapabilities(id))) candidates.add(id);
        }
        Collections.sort(candidates);
        return candidates.isEmpty()
                ? "No registered action is valid for this trigger."
                : "Actions valid for '" + trigger.getId() + "': " + String.join(", ", candidates) + ".";
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Keys must be registered triggers; each action must only require "
                + "capabilities its trigger provides.");
    }
}
