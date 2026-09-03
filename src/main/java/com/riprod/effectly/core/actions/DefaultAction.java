package com.riprod.effectly.core.actions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.Priority;

import javax.annotation.Nonnull;

public final class DefaultAction extends Action {

    @Nonnull
    public static final String TYPE = "Default";

    @Nonnull
    public static final BuilderCodec<DefaultAction> CODEC = BuilderCodec
            .builder(DefaultAction.class, DefaultAction::new, Action.BASE_CODEC)
            .build();

    public static void register() {
        Action.CODEC.register(Priority.DEFAULT, TYPE, DefaultAction.class, CODEC);
    }

    @Override
    public boolean execute(@Nonnull ActionContext context) {
        return false;
    }
}
