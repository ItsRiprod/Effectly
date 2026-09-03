package com.riprod.effectly.builtin.effects.itemmagnet;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialStructure;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.entity.system.ItemSpatialSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.utils.AbilityConditionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class ItemMagnetSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    private final Set<Dependency<EntityStore>> dependencies = Set.of(
            new SystemDependency<>(Order.AFTER, ItemSpatialSystem.class, OrderPriority.CLOSEST));

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return dependencies;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return ItemMagnetComponent.getComponentType();
    }

    @Override
    public boolean isParallel(int archetypeChunkSize, int taskCount) {
        return false;
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        World world = store.getExternalData().getWorld();
        if (world == null) return;

        Ref<EntityStore> playerRef = archetypeChunk.getReferenceTo(index);
        if (playerRef == null || !playerRef.isValid()) return;
        if (store.getArchetype(playerRef).contains(DeathComponent.getComponentType())) return;

        PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRefComponent == null) return;

        ItemMagnetComponent component = archetypeChunk.getComponent(index, ItemMagnetComponent.getComponentType());
        if (component == null) return;

        var active = AbilityConditionUtils.bestActiveForHandler(
                playerRef, store, world, ItemMagnetHandler.ID);
        if (active == null) return;

        double value = active.value();
        if (value < 1.0) return;

        TransformComponent playerTransform = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (playerTransform == null) return;
        Vector3d playerPosition = playerTransform.getPosition();

        ItemMagnetConfig config = active.configOrDefault(ItemMagnetConfig.class, ItemMagnetConfig.DEFAULTS);
        double radius = config.getBaseRange() * value;

        ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> itemSpatialResourceType =
                EntityModule.get().getItemSpatialResourceType();
        SpatialResource<Ref<EntityStore>, EntityStore> itemSpatial = store.getResource(itemSpatialResourceType);
        if (itemSpatial == null) return;

        SpatialStructure<Ref<EntityStore>> spatialStructure = itemSpatial.getSpatialStructure();
        if (spatialStructure == null) return;

        List<Ref<EntityStore>> items = new ArrayList<>();
        spatialStructure.ordered(playerPosition, radius, items);
        if (items.isEmpty()) return;

        double pickupThreshold = config.getPickupThreshold();
        double step = config.getLerpSpeed() * dt;

        for (Ref<EntityStore> itemRef : items) {
            if (itemRef == null || !itemRef.isValid()) continue;
            if (store.getComponent(itemRef, Interactable.getComponentType()) != null) continue;
            if (store.getComponent(itemRef, PreventPickup.getComponentType()) != null) continue;

            TransformComponent itemTransform = store.getComponent(itemRef, TransformComponent.getComponentType());
            if (itemTransform == null) continue;

            Vector3d itemPosition = itemTransform.getPosition();
            Vector3d direction = new Vector3d(playerPosition).sub(itemPosition);
            double distance = direction.length();
            if (distance < 1e-6 || distance <= pickupThreshold) continue;

            direction.mul(1.0 / distance);
            double moveAmount = Math.min(step, distance - pickupThreshold);
            itemPosition.add(direction.x * moveAmount, direction.y * moveAmount, direction.z * moveAmount);
        }
    }
}
