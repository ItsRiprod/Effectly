package com.hexvane.abilityapi.core;

import com.hexvane.abilityapi.ability.AbilityConditionSpec;
import com.hexvane.abilityapi.ability.AbilityType;
import com.hexvane.abilityapi.ability.AbilityValue;
import com.hexvane.abilityapi.core.asset.AbilityAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public final class AbilityEntry {

    @Nonnull
    public static final BuilderCodec<AbilityEntry> CODEC = BuilderCodec
            .builder(AbilityEntry.class, AbilityEntry::new)
            .append(new KeyedCodec<>("Value", Codec.DOUBLE),
                    (entry, v) -> entry.value = v,
                    entry -> entry.value)
            .documentation("Numeric value of the ability; binary abilities store 1 or 0")
            .add()
            .append(new KeyedCodec<>("Conditions",
                            new ArrayCodec<>(AbilityConditionSpec.CODEC, AbilityConditionSpec[]::new)),
                    (entry, v) -> entry.conditions = v == null ? new ArrayList<>() : new ArrayList<>(List.of(v)),
                    entry -> entry.conditions.toArray(AbilityConditionSpec[]::new))
            .documentation("Conditions that must all pass for this ability to be active")
            .add()
            .build();

    private double value = 1.0;
    private List<AbilityConditionSpec> conditions = new ArrayList<>();

    private AbilityEntry() {
    }

    public AbilityEntry(double value, @Nonnull List<AbilityConditionSpec> conditions) {
        this.value = value;
        this.conditions = new ArrayList<>(conditions);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Nonnull
    public List<AbilityConditionSpec> getConditions() {
        return conditions;
    }

    public void setConditions(@Nonnull List<AbilityConditionSpec> conditions) {
        this.conditions = new ArrayList<>(conditions);
    }

    @Nonnull
    public AbilityEntry copy() {
        return new AbilityEntry(value, conditions);
    }

    @Nonnull
    public AbilityValue toValue(@Nonnull String abilityId) {
        AbilityAsset asset = AbilityAsset.getAssetMap().getAsset(abilityId);
        if (asset != null && asset.getType() == AbilityType.BINARY) {
            return new AbilityValue(value != 0.0);
        }
        return new AbilityValue(value);
    }
}
