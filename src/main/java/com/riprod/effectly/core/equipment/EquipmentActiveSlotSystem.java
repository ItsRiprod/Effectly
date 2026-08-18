package com.riprod.effectly.core.equipment;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.InventorySetActiveSlotEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EquipmentActiveSlotSystem extends EntityEventSystem<EntityStore, InventorySetActiveSlotEvent> {

    public EquipmentActiveSlotSystem() {
        super(InventorySetActiveSlotEvent.class);
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull InventorySetActiveSlotEvent event) {
        int section = event.getInventorySectionId();
        if (section != InventoryComponent.HOTBAR_SECTION_ID
                && section != InventoryComponent.UTILITY_SECTION_ID
                && section != InventoryComponent.TOOLS_SECTION_ID) {
            return;
        }
        EquipmentAbilityComponent component = archetypeChunk.getComponent(
                index, EquipmentAbilityComponent.getComponentType());
        if (component == null) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null) return;

        EquipmentScan.reconcile(ref, store, commandBuffer, component);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return EquipmentAbilityComponent.getComponentType();
    }
}
