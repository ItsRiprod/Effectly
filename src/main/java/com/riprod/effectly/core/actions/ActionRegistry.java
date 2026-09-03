package com.riprod.effectly.core.actions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.effectly.core.actions.capability.CapabilityType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ActionRegistry {

    private static final Map<String, Class<? extends Action>> ACTIONS = new LinkedHashMap<>();

    private ActionRegistry() {}

    public static void reset() {
        ACTIONS.clear();
    }

    public static <T extends Action> void register(
            @Nonnull String id,
            @Nonnull Class<T> type,
            @Nonnull BuilderCodec<T> codec) {
        Class<? extends Action> existing = ACTIONS.putIfAbsent(id, type);
        if (existing != null && existing != type) {
            throw new IllegalArgumentException("duplicate action type id: " + id);
        }
        Action.CODEC.register(id, type, codec);
    }

    @Nullable
    public static Class<? extends Action> get(@Nonnull String id) {
        return ACTIONS.get(id);
    }

    @Nonnull
    public static Set<CapabilityType<?>> requiredCapabilities(@Nonnull String id) {
        var codec = Action.CODEC.getCodecFor(id);
        if (!(codec instanceof BuilderCodec<?> builder)) return Set.of();
        return builder.getSupplier().get() instanceof Action action
                ? action.requiredCapabilities()
                : Set.of();
    }

    @Nonnull
    public static Collection<String> ids() {
        return Collections.unmodifiableCollection(ACTIONS.keySet());
    }
}
