package com.riprod.effectly.builtin.conditions;

import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.riprod.effectly.ability.AbilityConditionSpec;
import com.riprod.effectly.core.condition.AbilityCondition;
import com.riprod.effectly.core.condition.AbilityConditionContext;
import com.riprod.effectly.zone.ZoneResolver;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class InZoneCondition implements AbilityCondition {

    public static final String ID = AbilityConditionSpec.TYPE_IN_ZONE;

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean test(@Nonnull AbilityConditionContext context, @Nonnull AbilityConditionSpec spec) {
        TransformComponent transform = context.getComponents()
                .getComponent(context.getRef(), TransformComponent.getComponentType());
        if (transform == null) return false;

        var position = transform.getPosition();
        int zone = ZoneResolver.getZoneAt(context.getWorld(),
                (int) Math.floor(position.x), (int) Math.floor(position.z));
        return spec.allowedZoneIds().contains(zone);
    }

    @Nonnull
    @Override
    public String keyword() {
        return "zone";
    }

    @Nullable
    @Override
    public Parsed parse(@Nonnull String[] remaining) {
        List<Integer> ids = new ArrayList<>();
        for (String argument : remaining) {
            try {
                int zone = Integer.parseInt(argument);
                if (zone < 0 || zone > 9999) break;
                ids.add(zone);
            } catch (NumberFormatException e) {
                break;
            }
        }
        if (ids.isEmpty()) return null;
        AbilityConditionSpec spec = ids.size() == 1
                ? new AbilityConditionSpec(ID, ids.get(0))
                : new AbilityConditionSpec(ID, ids.get(0), ids);
        return new Parsed(spec, ids.size());
    }

    @Nonnull
    @Override
    public String describe(@Nonnull AbilityConditionSpec spec) {
        List<Integer> zones = spec.allowedZoneIds();
        StringBuilder out = new StringBuilder("zone ");
        for (int i = 0; i < zones.size(); i++) {
            if (i > 0) out.append(' ');
            out.append(zones.get(i));
        }
        return out.toString();
    }

    @Nonnull
    @Override
    public String usage() {
        return "zone <id> [id...]";
    }

    @Nonnull
    @Override
    public String description() {
        return "active in world zone(s)";
    }
}
