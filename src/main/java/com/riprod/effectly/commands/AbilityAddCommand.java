package com.riprod.effectly.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.EffectlyPlugin;
import com.riprod.effectly.api.AbilityService;
import com.riprod.effectly.core.conditions.AbilityConditionSpec;
import com.riprod.effectly.core.conditions.components.AbilityDefinition;
import com.riprod.effectly.core.conditions.registry.ConditionRegistry;
import com.riprod.effectly.core.effects.utils.AbilityType;
import com.riprod.effectly.core.utils.AbilityRegistryUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * Grant an ability to a player.
 * Usage: /ability add &lt;ability_id&gt; [value] [condition key value...]
 * Example: /ability add stamina_regen 1.5 zone 3
 */
public class AbilityAddCommand extends AbstractPlayerCommand {
    private static final Pattern SPACES = Pattern.compile("\\s+");

    public AbilityAddCommand(@Nonnull EffectlyPlugin plugin) {
        super("add", "Grant an ability");
        this.setAllowsExtraArguments(true);
    }

    @Nonnull
    @Override
    public Message getUsageString(@Nonnull CommandSender sender) {
        return AbilityCommandHelp.addHelp(this);
    }

    @Nonnull
    @Override
    public Message getUsageShort(@Nonnull CommandSender sender, boolean fullyQualify) {
        return AbilityCommandHelp.usageShort(this, AbilityCommandHelp.ADD_USAGE, fullyQualify);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world) {
        String rawArgs = CommandUtil.stripCommandName(context.getInputString()).trim();
        if (rawArgs.startsWith("add")) {
            rawArgs = rawArgs.substring(3).trim();
        }
        String[] parts = SPACES.split(rawArgs, 3);
        if (parts.length < 1 || parts[0].isEmpty()) {
            context.sendMessage(AbilityCommandHelp.addHelp(this));
            return;
        }
        String abilityId = parts[0];
        AbilityDefinition def = AbilityRegistryUtils.get(abilityId);
        if (def == null) {
            context.sendMessage(Message.raw("Unknown ability: " + abilityId));
            return;
        }
        int valueEndIndex;
        Object value;
        if (def.type() == AbilityType.BINARY) {
            value = Boolean.TRUE;
            valueEndIndex = 1;
        } else {
            if (parts.length < 2 || parts[1].isEmpty()) {
                value = def.defaultValue();
                valueEndIndex = 1;
            } else {
                try {
                    double v = Double.parseDouble(parts[1]);
                    if (v < def.min() || v > def.max()) {
                        context.sendMessage(Message.raw("Value must be between " + def.min() + " and " + def.max()));
                        return;
                    }
                    value = v;
                    valueEndIndex = 2;
                } catch (NumberFormatException e) {
                    context.sendMessage(Message.raw("Invalid value: " + parts[1]));
                    return;
                }
            }
        }
        String conditionRest = valueEndIndex < parts.length ? String.join(" ", Arrays.asList(parts).subList(valueEndIndex, parts.length)) : null;
        AbilityCommandTargets.ParsedSuffix suffix = AbilityCommandTargets.stripTrailingPlayerName(conditionRest);
        PlayerRef targetPlayerRef = suffix.target() != null ? suffix.target() : playerRef;
        List<AbilityConditionSpec> conditions = parseConditions(suffix.remainder());
        AbilityService.setAbility(targetPlayerRef.getUuid(), abilityId, value,
                conditions != null ? conditions : List.of());

        boolean hasConditions = conditions != null && !conditions.isEmpty();
        boolean other = !targetPlayerRef.getUuid().equals(playerRef.getUuid());
        String targetLabel = other ? " to " + targetPlayerRef.getUsername() : "";
        if (def.type() == AbilityType.NUMERIC) {
            context.sendMessage(Message.raw("Granted " + abilityId + " with value " + value + targetLabel + (hasConditions ? " (with conditions)" : "")));
        } else {
            context.sendMessage(Message.raw("Granted " + abilityId + targetLabel + (hasConditions ? " (with conditions)" : "")));
        }
    }

    @Nonnull
    private static List<AbilityConditionSpec> parseConditions(String rest) {
        if (rest == null || rest.isBlank()) return List.of();
        return ConditionRegistry.parse(SPACES.split(rest.trim()));
    }
}
