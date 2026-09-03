package com.riprod.effectly.core.abilities.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public final class AbilityGrant {

    @Nonnull
    public static final BuilderCodec<AbilityGrant> CODEC = BuilderCodec
            .builder(AbilityGrant.class, AbilityGrant::new)
            .append(new KeyedCodec<>("Value", Codec.DOUBLE),
                    (grant, v) -> grant.value = v == null ? 1.0 : v,
                    grant -> grant.value)
            .documentation("Numeric value contributed by this source; binary abilities store 1 or 0")
            .add()
            .append(new KeyedCodec<>("Conditions",
                            new ArrayCodec<>(AbilityConditionSpec.CODEC, AbilityConditionSpec[]::new)),
                    (grant, v) -> grant.conditions = v == null ? new ArrayList<>() : new ArrayList<>(List.of(v)),
                    grant -> grant.conditions.isEmpty() ? null : grant.conditions.toArray(AbilityConditionSpec[]::new))
            .documentation("Conditions that must all pass for this source to contribute")
            .add()
            .build();

    private double value = 1.0;
    private List<AbilityConditionSpec> conditions = new ArrayList<>();


    private boolean persistent = true;

    private AbilityGrant() {
    }

    public AbilityGrant(double value, @Nonnull List<AbilityConditionSpec> conditions, boolean persistent) {
        this.value = value;
        this.conditions = new ArrayList<>(conditions);
        this.persistent = persistent;
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

    public boolean isPersistent() {
        return persistent;
    }

    @Nonnull
    public AbilityGrant copy() {
        return new AbilityGrant(value, conditions, persistent);
    }
}
