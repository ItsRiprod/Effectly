package com.riprod.effectly.core.equipment;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.inventory.ActiveSlotInventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.abilities.component.AbilityComponent;
import com.riprod.effectly.core.abilities.component.AbilityEntry;
import com.riprod.effectly.core.abilities.component.AbilityGrant;
import com.riprod.effectly.core.abilities.registry.AbilityContext;
import com.riprod.effectly.core.abilities.utils.AbilitySourcesUtils;
import com.riprod.effectly.core.utils.AbilityMutationUtils;

import java.util.LinkedHashMap;
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

        Map<String, Map<String, EquipmentGrant>> desired = new LinkedHashMap<>();
        collectArmor(store.getComponent(ref, InventoryComponent.Armor.getComponentType()), desired);
        // collectActive(store.getComponent(ref, InventoryComponent.Hotbar.getComponentType()), "Hotbar", desired);
        // collectActive(store.getComponent(ref, InventoryComponent.Utility.getComponentType()), "Utility", desired);
        // collectActive(store.getComponent(ref, InventoryComponent.Tool.getComponentType()), "Tool", desired);

        Map<String, Map<String, EquipmentGrant>> applied = component.getApplied();
        if (applied.equals(desired)) return;

        AbilityContext context = new AbilityContext(ref, writes, world, playerRef);
        for (Map.Entry<String, Map<String, EquipmentGrant>> source : desired.entrySet()) {
            Map<String, EquipmentGrant> before = applied.get(source.getKey());
            for (Map.Entry<String, EquipmentGrant> granted : source.getValue().entrySet()) {
                EquipmentGrant previous = before == null ? null : before.get(granted.getKey());
                if (granted.getValue().equals(previous)) continue;
                AbilityMutationUtils.grantIn(context, granted.getKey(), source.getKey(),
                        granted.getValue().value(), granted.getValue().conditions(), false);
            }
        }
        for (Map.Entry<String, Map<String, EquipmentGrant>> source : applied.entrySet()) {
            Map<String, EquipmentGrant> now = desired.get(source.getKey());
            for (String abilityId : source.getValue().keySet()) {
                if (now != null && now.containsKey(abilityId)) continue;
                AbilityMutationUtils.revokeIn(context, abilityId, source.getKey());
            }
        }
        component.setApplied(desired);
    }

    @Nonnull
    public static Map<String, Map<String, EquipmentGrant>> readExistingGrants(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store) {
        Map<String, Map<String, EquipmentGrant>> existing = new LinkedHashMap<>();
        AbilityComponent roster = AbilityComponent.of(ref, store);
        if (roster == null || roster.isEmpty()) return existing;

        for (Map.Entry<String, AbilityEntry> ability : roster.getAbilities().entrySet()) {
            for (Map.Entry<String, AbilityGrant> granted : ability.getValue().getGrants().entrySet()) {
                if (!AbilitySourcesUtils.isEquipment(granted.getKey())) continue;
                AbilityGrant grant = granted.getValue();
                existing.computeIfAbsent(granted.getKey(), id -> new LinkedHashMap<>())
                        .put(ability.getKey(), new EquipmentGrant(grant.getValue(), grant.getConditions()));
            }
        }
        return existing;
    }

    private static void collectArmor(
            @Nullable InventoryComponent.Armor armor,
            @Nonnull Map<String, Map<String, EquipmentGrant>> out) {
        if (armor == null) return;
        ItemContainer container = armor.getInventory();
        short capacity = container.getCapacity();
        for (short slot = 0; slot < capacity && slot < ItemArmorSlot.VALUES.length; slot++) {
            collectInto(container.getItemStack(slot), ItemArmorSlot.VALUES[slot].name(), out);
        }
    }

    private static void collectActive(
            @Nullable ActiveSlotInventoryComponent component,
            @Nonnull String slot,
            @Nonnull Map<String, Map<String, EquipmentGrant>> out) {
        if (component == null) return;
        collectInto(component.getActiveItem(), slot, out);
    }

    private static void collectInto(
            @Nullable ItemStack stack,
            @Nonnull String slot,
            @Nonnull Map<String, Map<String, EquipmentGrant>> out) {
        Map<String, EquipmentGrant> grants = new LinkedHashMap<>();
        EquipmentAbilitySource.collect(stack, grants);
        if (!grants.isEmpty()) out.put(AbilitySourcesUtils.equipment(slot), grants);
    }
}
