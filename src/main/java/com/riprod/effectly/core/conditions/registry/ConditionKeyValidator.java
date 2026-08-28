package com.riprod.effectly.core.conditions.registry;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

import javax.annotation.Nonnull;

public final class ConditionKeyValidator implements Validator<String> {

    public static final ConditionKeyValidator INSTANCE = new ConditionKeyValidator();

    private ConditionKeyValidator() {
    }

    @Override
    public void accept(String key, @Nonnull ValidationResults results) {
        if (key == null || key.isEmpty()) return;
        if (ConditionRegistry.get(key) == null) {
            results.fail("Unknown ability condition handler '" + key + "'. Registered: "
                    + String.join(", ", ConditionRegistry.ids()));
        }
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Must match a registered ability condition handler (EffectlyConditions).");
    }
}
