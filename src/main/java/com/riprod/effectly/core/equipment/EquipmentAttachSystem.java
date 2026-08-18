package com.riprod.effectly.core.equipment;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class EquipmentAttachSystem extends RefSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(InventoryComponent.Armor.getComponentType(), PlayerRef.getComponentType());
    }

    @Override
    public void onEntityAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull AddReason reason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        EquipmentAbilityComponent component = store.getComponent(
                ref, EquipmentAbilityComponent.getComponentType());
        if (component == null) {
            component = new EquipmentAbilityComponent();
            component.setApplied(EquipmentScan.readExistingGrants(ref, store));
            commandBuffer.putComponent(ref, EquipmentAbilityComponent.getComponentType(), component);
        }
        EquipmentScan.reconcile(ref, store, commandBuffer, component);
    }

    @Override
    public void onEntityRemove(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull RemoveReason reason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    }
}
