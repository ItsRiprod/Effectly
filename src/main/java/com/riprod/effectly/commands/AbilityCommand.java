package com.riprod.effectly.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.riprod.effectly.EffectlyPlugin;

import javax.annotation.Nonnull;

/**
 * Parent command for ability management.
 * Usage: /ability add|remove|list ...
 */
public class AbilityCommand extends AbstractCommandCollection {
    public AbilityCommand(@Nonnull EffectlyPlugin plugin) {
        super("ability", "Ability management commands");
        this.addSubCommand(new AbilityAddCommand(plugin));
        this.addSubCommand(new AbilityRemoveCommand(plugin));
        this.addSubCommand(new AbilityListCommand(plugin));
        this.addSubCommand(new AbilityAvailableCommand(plugin));
    }

    @Nonnull
    @Override
    public Message getUsageString(@Nonnull CommandSender sender) {
        return AbilityCommandHelp.rootHelp();
    }
}
