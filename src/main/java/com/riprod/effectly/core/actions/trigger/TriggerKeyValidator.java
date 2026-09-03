package com.riprod.effectly.core.actions.trigger;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

import javax.annotation.Nonnull;

public final class TriggerKeyValidator implements Validator<String> {

    public static final TriggerKeyValidator INSTANCE = new TriggerKeyValidator();

    private TriggerKeyValidator() {
    }

    @Override
    public void accept(String key, @Nonnull ValidationResults results) {
        if (key == null || key.isEmpty()) return;
        if (TriggerRegistry.get(key) == null) {
            results.fail("Unknown trigger '" + key + "'. Registered: "
                    + String.join(", ", TriggerRegistry.ids()));
        }
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Must match a registered trigger (EffectlyTriggers).");
    }
}
