package com.riprod.effectly.core.condition.asset;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.riprod.effectly.core.condition.AbilityConditions;

import javax.annotation.Nonnull;

public final class AbilityConditionKeyValidator implements Validator<String> {

    public static final AbilityConditionKeyValidator INSTANCE = new AbilityConditionKeyValidator();

    private AbilityConditionKeyValidator() {
    }

    @Override
    public void accept(String key, @Nonnull ValidationResults results) {
        if (key == null || key.isEmpty()) return;
        if (AbilityConditions.get(key) == null) {
            results.fail("Unknown ability condition handler '" + key + "'. Registered: "
                    + String.join(", ", AbilityConditions.ids()));
        }
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Must match a registered ability condition handler (EffectlyConditions).");
    }
}
