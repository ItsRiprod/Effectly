package com.riprod.abilityapi.builtin.conditions;

import com.riprod.abilityapi.ability.AbilityConditionSpec;
import com.riprod.abilityapi.config.AbilityApiConfig;
import com.riprod.abilityapi.core.condition.AbilityCondition;
import com.riprod.abilityapi.core.condition.AbilityConditionContext;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class InSunlightCondition implements AbilityCondition {

    public static final String ID = AbilityConditionSpec.TYPE_IN_SUNLIGHT;

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean test(@Nonnull AbilityConditionContext context, @Nonnull AbilityConditionSpec spec) {
        WorldTimeResource worldTime = context.getComponents().getResource(WorldTimeResource.getResourceType());
        if (worldTime == null) return false;

        AbilityApiConfig config = AbilityApiConfig.get();
        if (worldTime.getSunlightFactor() < config.getSunlightMinFactor()) return false;

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
        int effective = (int) (skyLight * worldTime.getSunlightFactor());
        return effective >= config.getSunlightMinEffective();
    }

    @Nonnull
    @Override
    public String keyword() {
        return "sunlight";
    }

    @Nullable
    @Override
    public Parsed parse(@Nonnull String[] remaining) {
        return new Parsed(new AbilityConditionSpec(ID, 0), 0);
    }

    @Nonnull
    @Override
    public String describe(@Nonnull AbilityConditionSpec spec) {
        return "sunlight";
    }

    @Nonnull
    @Override
    public String usage() {
        return "sunlight";
    }

    @Nonnull
    @Override
    public String description() {
        return "active in open sunlight during daytime";
    }
}
