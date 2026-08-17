package com.riprod.abilityapi.systems;

import com.riprod.abilityapi.core.AbilityContext;
import com.riprod.abilityapi.core.stat.AbilityStatApplier;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class AbilityStatService {

    private AbilityStatService() {}

    public static void applyForPlayer(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> store,
            @Nonnull World world) {
        if (!ref.isValid()) return;

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        AbilityStatApplier.apply(new AbilityContext(ref, store, world, playerRef));
    }
}
