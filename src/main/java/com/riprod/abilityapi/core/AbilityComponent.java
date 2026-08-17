package com.riprod.abilityapi.core;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.abilityapi.core.asset.AbilityHandlerConfig;
import com.riprod.abilityapi.core.asset.AbilityAsset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbilityComponent implements Component<EntityStore> {

    protected String abilityId;

    @Nonnull
    @Override
    public abstract Component<EntityStore> clone();

    @Nullable
    public String getAbilityId() {
        return this.abilityId;
    }

    public void bind(@Nonnull String abilityId) {
        this.abilityId = abilityId;
    }

    @Nullable
    public <T extends AbilityHandlerConfig> T config(@Nonnull Class<T> type) {
        return this.abilityId == null ? null : AbilityAsset.configFor(this.abilityId, type);
    }

    @Nonnull
    public <T extends AbilityHandlerConfig> T configOrDefault(@Nonnull Class<T> type, @Nonnull T fallback) {
        T config = config(type);
        return config != null ? config : fallback;
    }
}
