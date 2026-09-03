package com.riprod.effectly.builtin.capabilities;

import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.riprod.effectly.core.actions.capability.Capability;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class DamageCapability implements Capability {

    public static final CapabilityType<DamageCapability> TYPE =
            new CapabilityType<>("Damage", DamageCapability.class);

    private final Damage damage;
    private DamageCause cause;
    private boolean causeResolved;

    public DamageCapability(@Nonnull Damage damage) {
        this.damage = damage;
    }

    @Nonnull
    public Damage getDamage() {
        return damage;
    }

    @Nullable
    public DamageCause getCause() {
        if (!causeResolved) {
            causeResolved = true;
            cause = DamageCause.getAssetMap().getAsset(damage.getDamageCauseIndex());
        }
        return cause;
    }
}
