package com.riprod.effectly.core.abilities.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.core.effects.registry.EffectAsset;
import com.riprod.effectly.core.effects.registry.EffectHandlerConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractAbilityComponent implements Component<EntityStore> {

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
    public <T extends EffectHandlerConfig> T config(@Nonnull Class<T> type) {
        return this.abilityId == null ? null : EffectAsset.configFor(this.abilityId, type);
    }

    @Nonnull
    public <T extends EffectHandlerConfig> T configOrDefault(@Nonnull Class<T> type, @Nonnull T fallback) {
        T config = config(type);
        return config != null ? config : fallback;
    }
}
