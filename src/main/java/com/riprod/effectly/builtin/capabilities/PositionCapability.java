package com.riprod.effectly.builtin.capabilities;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.riprod.effectly.core.actions.capability.Capability;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3d;
import org.joml.Vector3i;

public final class PositionCapability implements Capability {

    public static final CapabilityType<PositionCapability> TYPE =
            new CapabilityType<>("Position", PositionCapability.class);

    private final Vector3d position;
    private Vector3i block;
    private BlockType blockType;
    private boolean blockResolved;

    public PositionCapability(@Nonnull Vector3d position) {
        this.position = position;
    }

    public PositionCapability(@Nonnull Vector3i block, @Nonnull BlockType blockType) {
        this.position = new Vector3d(block.x() + 0.5, block.y(), block.z() + 0.5);
        this.block = block;
        this.blockType = blockType;
        this.blockResolved = true;
    }

    @Nonnull
    public Vector3d getPosition() {
        return position;
    }

    @Nonnull
    public Vector3i getBlockPosition() {
        if (block == null) {
            block = new Vector3i(
                    MathUtil.floor(position.x), MathUtil.floor(position.y), MathUtil.floor(position.z));
        }
        return block;
    }

    @Nullable
    public BlockType resolveBlock(@Nonnull ChunkStore chunks) {
        if (blockResolved) return blockType;
        blockResolved = true;

        Vector3i at = getBlockPosition();
        if (at.y() < ChunkUtil.MIN_Y || at.y() >= ChunkUtil.HEIGHT) return null;

        var sectionRef = chunks.getChunkSectionReferenceAtBlock(at.x(), at.y(), at.z());
        if (sectionRef == null || !sectionRef.isValid()) return null;

        BlockSection section = chunks.getStore().getComponent(sectionRef, BlockSection.getComponentType());
        if (section == null) return null;

        int blockId = section.get(at.x(), at.y(), at.z());
        blockType = blockId != 0 ? BlockType.getAssetMap().getAsset(blockId) : null;
        return blockType;
    }
}
