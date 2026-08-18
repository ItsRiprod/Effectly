package com.riprod.effectly.core.equipment;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.InventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.ActiveSlotInventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EquipmentChangeSystem extends EntityEventSystem<EntityStore, InventoryChangeEvent> {

    public EquipmentChangeSystem() {
        super(InventoryChangeEvent.class);
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull InventoryChangeEvent event) {
        if (!affectsEquipment(event)) return;

        EquipmentAbilityComponent component = archetypeChunk.getComponent(
                index, EquipmentAbilityComponent.getComponentType());
        if (component == null) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null) return;

        EquipmentScan.reconcile(ref, store, commandBuffer, component);
    }

    private static boolean affectsEquipment(@Nonnull InventoryChangeEvent event) {
        ComponentType<EntityStore, ? extends InventoryComponent> type = event.getComponentType();
        return type == InventoryComponent.Armor.getComponentType();

        // if (type != InventoryComponent.Hotbar.getComponentType()
        //         && type != InventoryComponent.Utility.getComponentType()
        //         && type != InventoryComponent.Tool.getComponentType()) {
        //     return false;
        // }
        // if (!(event.getInventory() instanceof ActiveSlotInventoryComponent active)) return false;
        // return event.getTransaction().wasSlotModified(active.getActiveSlot());
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return EquipmentAbilityComponent.getComponentType();
    }
}
