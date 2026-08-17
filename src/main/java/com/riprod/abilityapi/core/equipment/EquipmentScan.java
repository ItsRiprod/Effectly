package com.riprod.abilityapi.core.equipment;

import com.riprod.abilityapi.core.AbilityContext;
import com.riprod.abilityapi.core.AbilityEntry;
import com.riprod.abilityapi.core.AbilityGrant;
import com.riprod.abilityapi.core.AbilityMutations;
import com.riprod.abilityapi.core.AbilityRoster;
import com.riprod.abilityapi.core.AbilitySources;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.ActiveSlotInventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EquipmentScan {

    private EquipmentScan() {}

    public static void reconcile(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull ComponentAccessor<EntityStore> writes,
            @Nonnull EquipmentAbilityComponent component) {
        if (!ref.isValid()) return;

        World world = store.getExternalData().getWorld();
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (world == null || playerRef == null) return;

        Map<String, Double> desired = new LinkedHashMap<>();
        collectArmor(store.getComponent(ref, InventoryComponent.Armor.getComponentType()), desired);
        collectActive(store.getComponent(ref, InventoryComponent.Hotbar.getComponentType()), desired);
        collectActive(store.getComponent(ref, InventoryComponent.Utility.getComponentType()), desired);
        collectActive(store.getComponent(ref, InventoryComponent.Tool.getComponentType()), desired);

        Map<String, Double> applied = component.getApplied();
        if (applied.equals(desired)) return;

        AbilityContext context = new AbilityContext(ref, writes, world, playerRef);
        for (Map.Entry<String, Double> granted : desired.entrySet()) {
            Double previous = applied.get(granted.getKey());
            if (previous != null && previous.doubleValue() == granted.getValue().doubleValue()) continue;
            AbilityMutations.grantIn(context, granted.getKey(), AbilitySources.EQUIPMENT,
                    granted.getValue(), List.of(), false);
        }
        for (String abilityId : applied.keySet()) {
            if (desired.containsKey(abilityId)) continue;
            AbilityMutations.revokeIn(context, abilityId, AbilitySources.EQUIPMENT);
        }
        component.setApplied(desired);
    }

    @Nonnull
    public static Map<String, Double> readExistingGrants(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store) {
        AbilityRoster roster = AbilityRoster.of(ref, store);
        if (roster == null || roster.isEmpty()) return new LinkedHashMap<>();

        Map<String, Double> existing = new LinkedHashMap<>();
        for (Map.Entry<String, AbilityEntry> granted : roster.getAbilities().entrySet()) {
            AbilityGrant grant = granted.getValue().getGrant(AbilitySources.EQUIPMENT);
            if (grant != null) existing.put(granted.getKey(), grant.getValue());
        }
        return existing;
    }

    private static void collectArmor(
            @Nullable InventoryComponent.Armor armor,
            @Nonnull Map<String, Double> out) {
        if (armor == null) return;
        ItemContainer container = armor.getInventory();
        short capacity = container.getCapacity();
        for (short slot = 0; slot < capacity; slot++) {
            EquipmentAbilitySource.collect(container.getItemStack(slot), out);
        }
    }

    private static void collectActive(
            @Nullable ActiveSlotInventoryComponent component,
            @Nonnull Map<String, Double> out) {
        if (component == null) return;
        EquipmentAbilitySource.collect(component.getActiveItem(), out);
    }
}
