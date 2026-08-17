package com.riprod.abilityapi.commands;

import com.riprod.abilityapi.AbilityAPIPlugin;
import com.riprod.abilityapi.ability.AbilityConditionSpec;
import com.riprod.abilityapi.ability.AbilityValue;
import com.riprod.abilityapi.core.AbilityGrant;
import com.riprod.abilityapi.core.AbilityRosters;
import com.riprod.abilityapi.core.condition.AbilityConditions;
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
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * List abilities for a player.
 * Usage: /ability list [player]
 */
public class AbilityListCommand extends AbstractPlayerCommand {

    public AbilityListCommand(@Nonnull AbilityAPIPlugin plugin) {
        super("list", "List your abilities");
        this.setAllowsExtraArguments(true);
    }

    @Nonnull
    @Override
    public Message getUsageString(@Nonnull CommandSender sender) {
        return AbilityCommandHelp.listHelp(this);
    }

    @Nonnull
    @Override
    public Message getUsageShort(@Nonnull CommandSender sender, boolean fullyQualify) {
        return AbilityCommandHelp.usageShort(this, AbilityCommandHelp.LIST_USAGE, fullyQualify);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world) {
        String rawArgs = CommandUtil.stripCommandName(context.getInputString()).trim();
        if (rawArgs.startsWith("list")) {
            rawArgs = rawArgs.substring(4).trim();
        }
        PlayerRef targetPlayerRef = playerRef;
        if (!rawArgs.isEmpty()) {
            PlayerRef found = AbilityCommandTargets.findOnlinePlayer(rawArgs);
            if (found == null) {
                context.sendMessage(Message.raw("Unknown player: " + rawArgs));
                return;
            }
            targetPlayerRef = found;
        }
        Map<String, AbilityValue> abilities = AbilityRosters.readAll(targetPlayerRef.getUuid());
        boolean other = !targetPlayerRef.getUuid().equals(playerRef.getUuid());
        if (abilities.isEmpty()) {
            context.sendMessage(Message.raw(other ? targetPlayerRef.getUsername() + " has no abilities granted." : "No abilities granted."));
            return;
        }
        Map<String, Map<String, AbilityGrant>> grants = AbilityRosters.readAllGrants(targetPlayerRef.getUuid());

        context.sendMessage(Message.raw(other
                ? targetPlayerRef.getUsername() + "'s abilities:"
                : "Abilities:"));
        for (Map.Entry<String, AbilityValue> ability : abilities.entrySet()) {
            context.sendMessage(Message.raw(formatAbility(
                    ability.getKey(), ability.getValue(), grants.get(ability.getKey()))));
        }
    }

    @Nonnull
    private static String formatAbility(
            @Nonnull String abilityId,
            @Nullable AbilityValue value,
            @Nullable Map<String, AbilityGrant> grants) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ").append(abilityId);
        if (value != null && value.getRaw() instanceof Number n) {
            sb.append(" = ").append(formatNumber(n.doubleValue()));
        }
        if (grants == null || grants.isEmpty()) return sb.toString();

        if (grants.size() == 1) {
            appendConditions(sb, grants.values().iterator().next().getConditions());
            return sb.toString();
        }
        sb.append("  [");
        boolean first = true;
        for (Map.Entry<String, AbilityGrant> granted : grants.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(granted.getKey()).append("=").append(formatNumber(granted.getValue().getValue()));
            appendConditions(sb, granted.getValue().getConditions());
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private static void appendConditions(
            @Nonnull StringBuilder sb,
            @Nonnull List<AbilityConditionSpec> conditions) {
        if (conditions.isEmpty()) return;
        sb.append(" (");
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) sb.append("; ");
            sb.append(AbilityConditions.describe(conditions.get(i)));
        }
        sb.append(")");
    }

    @Nonnull
    private static String formatNumber(double value) {
        return value == (long) value ? String.valueOf((long) value) : String.valueOf(value);
    }


    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

}
