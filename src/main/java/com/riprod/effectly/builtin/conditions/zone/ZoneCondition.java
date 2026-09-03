package com.riprod.effectly.builtin.conditions.zone;

import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;
import com.riprod.effectly.core.conditions.ConditionContext;
import com.riprod.effectly.core.conditions.registry.ConditionAsset;
import com.riprod.effectly.core.conditions.registry.ConditionHandler;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ZoneCondition implements ConditionHandler {

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
            @Nonnull ConditionContext context,
            @Nonnull ConditionAsset asset,
            @Nonnull AbilityConditionSpec spec) {
        int[] allowed = allowedZones(asset, spec);
        if (allowed.length == 0) return false;

        TransformComponent transform = context.getComponents()
                .getComponent(context.getRef(), TransformComponent.getComponentType());
        if (transform == null) return false;

        var position = transform.getPosition();
        int zone = ZoneResolver.getZoneAt(context.getWorld(), (int) position.x, (int) position.z);
        for (int allowedZone : allowed) {
            if (allowedZone == zone) return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Parsed parse(@Nonnull ConditionAsset asset, @Nonnull String[] remaining) {
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
    public String describe(@Nonnull ConditionAsset asset, @Nonnull AbilityConditionSpec spec) {
        StringBuilder out = new StringBuilder(asset.getKeyword());
        for (int zone : allowedZones(asset, spec)) {
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
    private static int[] allowedZones(
            @Nonnull ConditionAsset asset, @Nonnull AbilityConditionSpec spec) {
        int[] override = spec.allowedZoneIdArray();
        if (override.length > 0) return override;
        return asset.configOrDefault(ZoneConditionConfig.class, ZoneConditionConfig.DEFAULTS).getZones();
    }
}
