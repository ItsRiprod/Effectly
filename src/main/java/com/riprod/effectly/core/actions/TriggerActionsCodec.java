package com.riprod.effectly.core.actions;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.riprod.effectly.core.actions.capability.CapabilityType;
import com.riprod.effectly.core.actions.trigger.Trigger;
import com.riprod.effectly.core.actions.trigger.TriggerRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.Nonnull;

public final class TriggerActionsCodec extends MapCodec<Action[], LinkedHashMap<String, Action[]>> {

    public TriggerActionsCodec() {
        super(new ArrayCodec<>(Action.CODEC, Action[]::new), LinkedHashMap::new);
    }

    @Nonnull
    @Override
    public Schema toSchema(@Nonnull SchemaContext context) {
        Action.CODEC.toSchema(context);

        var schema = new ObjectSchema();
        schema.setTitle("Actions by Trigger");
        schema.setAdditionalProperties(false);

        var names = new StringSchema();
        names.setEnum(TriggerRegistry.ids().toArray(String[]::new));
        schema.setPropertyNames(names);

        var properties = new LinkedHashMap<String, Schema>();
        schema.setProperties(properties);

        for (Trigger trigger : TriggerRegistry.all()) {
            var actions = new ArraySchema(actionSelector(context, trigger));
            actions.setMarkdownDescription("Actions fired by " + trigger.getId()
                    + ". Provides capabilities: " + capabilityIds(trigger));
            properties.put(trigger.getId(), actions);
        }
        return schema;
    }

    @Nonnull
    private static Schema actionSelector(@Nonnull SchemaContext context, @Nonnull Trigger trigger) {
        var ids = new ArrayList<>(Action.CODEC.getRegisteredIds());
        Collections.sort(ids);

        var defaultCodec = Action.CODEC.getDefaultCodec();
        var options = new ArrayList<Schema>();
        var values = new ArrayList<String>();
        String defaultKey = null;

        for (String id : ids) {
            if (!trigger.getProvides().containsAll(ActionRegistry.requiredCapabilities(id))) {
                continue;
            }
            var codec = Action.CODEC.getCodecFor(id);
            if (codec == defaultCodec) defaultKey = id;
            options.add(context.refDefinition(codec));
            values.add(id);
        }

        var selector = Schema.anyOf(options.toArray(Schema[]::new));
        selector.getHytale().setMergesProperties(true);
        selector.setTitle("Type Selector");
        selector.setHytaleSchemaTypeField(new Schema.SchemaTypeField(
                Action.CODEC.getKey(), defaultKey, values.toArray(String[]::new)));
        return selector;
    }

    @Nonnull
    private static String capabilityIds(@Nonnull Trigger trigger) {
        List<String> ids = new ArrayList<>();
        for (CapabilityType<?> capability : trigger.getProvides()) {
            ids.add(capability.getId());
        }
        Collections.sort(ids);
        return String.join(", ", ids);
    }
}
