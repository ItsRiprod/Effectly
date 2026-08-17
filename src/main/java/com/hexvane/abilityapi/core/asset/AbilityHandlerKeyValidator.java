package com.hexvane.abilityapi.core.asset;

import com.hexvane.abilityapi.core.AbilityHandlerRegistry;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import javax.annotation.Nonnull;

public final class AbilityHandlerKeyValidator implements Validator<String> {

    public static final AbilityHandlerKeyValidator INSTANCE = new AbilityHandlerKeyValidator();

    private AbilityHandlerKeyValidator() {
    }

    @Override
    public void accept(String key, @Nonnull ValidationResults results) {
        if (key == null || key.isEmpty()) return;
        if (AbilityHandlerRegistry.get(key) == null) {
            results.fail("Unknown ability handler '" + key + "'. Registered: "
                    + String.join(", ", AbilityHandlerRegistry.ids()));
        }
    }

    @Override
    public void updateSchema(SchemaContext context, @Nonnull Schema target) {
        target.setDescription("Must match a registered ability handler (AbilityAPIHandlers).");
    }
}
