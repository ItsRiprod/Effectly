package com.riprod.effectly.builtin.conditions.skylight;

import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;
import com.riprod.effectly.core.conditions.ConditionContext;
import com.riprod.effectly.core.conditions.registry.ConditionAsset;
import com.riprod.effectly.core.conditions.registry.ConditionHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SkyLightCondition implements ConditionHandler {

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
            @Nonnull ConditionContext context,
            @Nonnull ConditionAsset asset,
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

        var chunkStore = context.getWorld().getChunkStore();
        var sectionRef = chunkStore.getChunkSectionReferenceAtBlock(blockX, blockY, blockZ);
        if (sectionRef == null || !sectionRef.isValid()) return false;

        BlockSection section = chunkStore.getStore().getComponent(sectionRef, BlockSection.getComponentType());
        if (section == null) return false;

        byte skyLight = section.getGlobalLight().getSkyLight(blockX, blockY, blockZ);
        if (skyLight < config.getMinSkyLight()) return false;
        if (skyLight > config.getMaxSkyLight()) return false;

        int effective = (int) (skyLight * sunlightFactor);
        return effective >= config.getMinEffectiveLight();
    }

    @Nullable
    @Override
    public Parsed parse(@Nonnull ConditionAsset asset, @Nonnull String[] remaining) {
        return new Parsed(new AbilityConditionSpec(asset.getId()), 0);
    }
}
