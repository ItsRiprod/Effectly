package com.riprod.effectly.builtin.capabilities;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.actions.capability.Capability;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import javax.annotation.Nonnull;

public final class OtherEntityCapability implements Capability {

    public static final CapabilityType<OtherEntityCapability> TYPE =
            new CapabilityType<>("Other_Entity", OtherEntityCapability.class);

    private final Ref<EntityStore> entity;

    public OtherEntityCapability(@Nonnull Ref<EntityStore> entity) {
        this.entity = entity;
    }

    @Nonnull
    public Ref<EntityStore> getEntity() {
        return entity;
    }
}
