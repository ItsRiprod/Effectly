package com.riprod.effectly.builtin.conditions;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.riprod.effectly.ability.AbilityConditionSpec;
import com.riprod.effectly.core.condition.AbilityCondition;
import com.riprod.effectly.core.condition.AbilityConditionContext;
import com.riprod.effectly.core.condition.asset.AbilityConditionAsset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SkyLightCondition implements AbilityCondition {

    public static final String ID = "sky_light";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Nonnull
    @Override
    public ConfigBinding<SkyLightConditionConfig> getConfigBinding() {
        return ConfigBinding.of(SkyLightConditionConfig.class, SkyLightConditionConfig.CODEC);
    }

    @Override
    public boolean test(
            @Nonnull AbilityConditionContext context,
            @Nonnull AbilityConditionAsset asset,
            @Nonnull AbilityConditionSpec spec) {
        WorldTimeResource worldTime = context.getComponents().getResource(WorldTimeResource.getResourceType());
        if (worldTime == null) return false;

        SkyLightConditionConfig config =
                asset.configOrDefault(SkyLightConditionConfig.class, SkyLightConditionConfig.DEFAULTS);

        double sunlightFactor = worldTime.getSunlightFactor();
        if (sunlightFactor < config.getMinSunlightFactor()) return false;
        if (sunlightFactor > config.getMaxSunlightFactor()) return false;

        TransformComponent transform = context.getComponents()
                .getComponent(context.getRef(), TransformComponent.getComponentType());
        if (transform == null) return false;

        var position = transform.getPosition();
        int blockX = (int) Math.floor(position.x);
        int blockY = (int) Math.floor(position.y);
        int blockZ = (int) Math.floor(position.z);

        WorldChunk chunk = context.getWorld().getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(blockX, blockZ));
        if (chunk == null) return false;

        byte skyLight = chunk.getBlockChunk().getSkyLight(blockX, blockY, blockZ);
        if (skyLight < config.getMinSkyLight()) return false;
        if (skyLight > config.getMaxSkyLight()) return false;

        int effective = (int) (skyLight * sunlightFactor);
        return effective >= config.getMinEffectiveLight();
    }

    @Nullable
    @Override
    public Parsed parse(@Nonnull AbilityConditionAsset asset, @Nonnull String[] remaining) {
        return new Parsed(new AbilityConditionSpec(asset.getId()), 0);
    }
}
