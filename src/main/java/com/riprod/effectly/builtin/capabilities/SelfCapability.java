package com.riprod.effectly.builtin.capabilities;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.actions.capability.Capability;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import javax.annotation.Nonnull;

public final class SelfCapability implements Capability {

    public static final CapabilityType<SelfCapability> TYPE =
            new CapabilityType<>("Self", SelfCapability.class);

    private final Ref<EntityStore> entity;

    public SelfCapability(@Nonnull Ref<EntityStore> entity) {
        this.entity = entity;
    }

    @Nonnull
    public Ref<EntityStore> getEntity() {
        return entity;
    }
}
