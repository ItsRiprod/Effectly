package com.riprod.effectly.builtin.conditions;

import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.riprod.effectly.ability.AbilityConditionSpec;
import com.riprod.effectly.core.condition.AbilityCondition;
import com.riprod.effectly.core.condition.AbilityConditionContext;
import com.riprod.effectly.core.condition.asset.AbilityConditionAsset;
import com.riprod.effectly.zone.ZoneResolver;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ZoneCondition implements AbilityCondition {

    public static final String ID = "zone";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<ZoneConditionConfig> getConfigBinding() {
        return ConfigBinding.of(ZoneConditionConfig.class, ZoneConditionConfig.CODEC);
    }

    @Override
    public boolean test(
            @Nonnull AbilityConditionContext context,
            @Nonnull AbilityConditionAsset asset,
            @Nonnull AbilityConditionSpec spec) {
        List<Integer> allowed = allowedZones(asset, spec);
        if (allowed.isEmpty()) return false;

        TransformComponent transform = context.getComponents()
                .getComponent(context.getRef(), TransformComponent.getComponentType());
        if (transform == null) return false;

        var position = transform.getPosition();
        int zone = ZoneResolver.getZoneAt(context.getWorld(),
                (int) Math.floor(position.x), (int) Math.floor(position.z));
        return allowed.contains(zone);
    }

    @Nullable
    @Override
    public Parsed parse(@Nonnull AbilityConditionAsset asset, @Nonnull String[] remaining) {
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
        if (ids.isEmpty()) {
            return new Parsed(new AbilityConditionSpec(asset.getId()), 0);
        }
        AbilityConditionSpec spec = ids.size() == 1
                ? new AbilityConditionSpec(asset.getId(), ids.get(0))
                : new AbilityConditionSpec(asset.getId(), ids.get(0), ids);
        return new Parsed(spec, ids.size());
    }

    @Nonnull
    @Override
    public String describe(@Nonnull AbilityConditionAsset asset, @Nonnull AbilityConditionSpec spec) {
        List<Integer> zones = allowedZones(asset, spec);
        StringBuilder out = new StringBuilder(asset.getKeyword());
        for (Integer zone : zones) {
            out.append(' ').append(zone);
        }
        return out.toString();
    }

    @Nonnull
    @Override
    public String argumentUsage() {
        return "[id...]";
    }

    @Nonnull
    private static List<Integer> allowedZones(
            @Nonnull AbilityConditionAsset asset, @Nonnull AbilityConditionSpec spec) {
        List<Integer> override = spec.allowedZoneIds();
        if (!override.isEmpty()) return override;
        return asset.configOrDefault(ZoneConditionConfig.class, ZoneConditionConfig.DEFAULTS).getZones();
    }
}
