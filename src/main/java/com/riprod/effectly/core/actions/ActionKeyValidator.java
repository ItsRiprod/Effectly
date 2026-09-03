package com.riprod.effectly.core.actions;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

import javax.annotation.Nonnull;

public final class ActionKeyValidator implements Validator<String> {

    public static final ActionKeyValidator INSTANCE = new ActionKeyValidator();

    private ActionKeyValidator() {
    }

    @Override
    public void accept(String key, @Nonnull ValidationResults results) {
        if (key == null || key.isEmpty()) return;
        if (ActionRegistry.get(key) == null) {
            results.fail("Unknown action type '" + key + "'. Registered: "
                    + String.join(", ", ActionRegistry.ids()));
        }
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Must match a registered action type (EffectlyActions).");
    }
}
