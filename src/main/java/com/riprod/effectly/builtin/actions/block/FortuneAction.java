package com.riprod.effectly.builtin.actions.block;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockBreakingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockGathering;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.HarvestingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.PhysicsDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.SoftBlockDropType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.builtin.capabilities.PositionCapability;
import com.riprod.effectly.core.actions.Action;
import com.riprod.effectly.core.actions.ActionContext;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class FortuneAction extends Action {

    @Nonnull
    public static final String ID = "Fortune";

    private static final Set<CapabilityType<?>> REQUIRED = Set.of(PositionCapability.TYPE);

    @Nonnull
    public static final BuilderCodec<FortuneAction> CODEC = BuilderCodec
            .builder(FortuneAction.class, FortuneAction::new, Action.BASE_CODEC)
            .append(new KeyedCodec<>("AffectedBlocks",
                            new SetCodec<>(Codec.STRING, LinkedHashSet::new, false)),
                    (action, v) -> action.affectedBlocks = v == null ? Set.of() : v,
                    action -> action.affectedBlocks.isEmpty() ? null : action.affectedBlocks)
            .documentation("Block type ids this applies to. Empty means every broken block")
            .add()
            .build();

    private Set<String> affectedBlocks = Set.of();

    @Nonnull
    @Override
    public Set<CapabilityType<?>> requiredCapabilities() {
        return REQUIRED;
    }

    @Override
    public boolean execute(@Nonnull ActionContext context, double value) {
        int rolls = (int) value;
        if (rolls < 1) return false;

        PositionCapability where = context.get(PositionCapability.TYPE);
        BlockType blockType = where.resolveBlock(context.getChunkStore());
        if (blockType == null) return false;

        if (!affectedBlocks.isEmpty() && !affectedBlocks.contains(blockType.getId())) return false;

        DropParams params = dropParamsFor(blockType);
        if (params == null) return false;

        var store = context.getEntityStore();
        boolean dropped = false;
        for (int i = 0; i < rolls; i++) {
            List<ItemStack> drops = BlockHarvestUtils.getDrops(
                    blockType, params.quantity(), params.itemId(), params.dropListId());
            if (drops.isEmpty()) continue;

            Holder<EntityStore>[] holders = ItemComponent.generateItemDrops(
                    store, drops, where.getPosition(), Rotation3f.ZERO);
            if (holders != null && holders.length > 0) {
                store.addEntities(holders, AddReason.SPAWN);
                dropped = true;
            }
        }
        return dropped;
    }

    @Nullable
    private static DropParams dropParamsFor(@Nonnull BlockType blockType) {
        BlockGathering gathering = blockType.getGathering();
        if (gathering == null) return null;

        PhysicsDropType physics = gathering.getPhysics();
        BlockBreakingDropType breaking = gathering.getBreaking();
        SoftBlockDropType soft = gathering.getSoft();
        HarvestingDropType harvest = gathering.getHarvest();

        int quantity = 1;
        String itemId = null;
        String dropListId = null;

        if (physics != null) {
            itemId = physics.getItemId();
            dropListId = physics.getDropListId();
        } else if (breaking != null) {
            quantity = breaking.getQuantity();
            itemId = breaking.getItemId();
            dropListId = breaking.getDropListId();
        } else if (soft != null) {
            itemId = soft.getItemId();
            dropListId = soft.getDropListId();
        } else if (harvest != null) {
            itemId = harvest.getItemId();
            dropListId = harvest.getDropListId();
        }

        if (itemId == null && dropListId == null) return null;
        return new DropParams(quantity, itemId, dropListId);
    }

    private record DropParams(int quantity, @Nullable String itemId, @Nullable String dropListId) {}
}
