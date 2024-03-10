package net.simplyvanilla.simplynicks.commands;

import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.database.TeamMySQL;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TeamCommandExecutor implements CommandExecutor {
    private final SimplyNicks plugin;

    public TeamCommandExecutor(SimplyNicks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args) {
        if (!(sender instanceof ConsoleCommandSender) && !(sender instanceof RemoteConsoleCommandSender)) {
            return false;
        }

        if (args.length == 0) {
            return false;
        }

        String subCommand = args[0];
        switch (subCommand.toLowerCase(Locale.ROOT)) {
            case "create": {
                if (args.length < 2) {
                    return false;
                }
                String name = args[1];
                return handleCreate(sender, name);
            }
            case "modify": {
                if (args.length < 3) {
                    return false;
                }
                String name = args[1];
                String modifyType = args[2];
                String value = args.length > 3 ? args[3] : "";
                return handleModify(sender, name, modifyType, value);
            }
            case "join": {
                if (args.length < 3) {
                    return false;
                }
                String member = args[1];
                String name = args[2];
                return handleJoin(sender, member, name);
            }
            case "leave": {
                if (args.length < 3) {
                    return false;
                }
                String leaver = args[1];
                String name = args[2];
                return handleLeave(sender, leaver, name);
            }
            case "delete":
                if (args.length < 2) {
                    return false;
                }
                String name = args[1];
                return handleDelete(sender, name);
        }

        return false;
    }

    private UUID getPlayerId(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(input);
            if (!player.hasPlayedBefore() || player.getName() == null) {
                return null;
            }
            return player.getUniqueId();
        }
    }

    // /team delete <name>
    private boolean handleDelete(CommandSender sender, String name) {
        int teamId = this.plugin.getTeamDatabase().getTeamId(name);
        if (teamId < 0) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotFound");
            return false;
        }

        if (!this.plugin.getTeamDatabase().deleteTeam(teamId)) {
            return false;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamDeletedMessage");
        return true;
    }

    // /team leave <leaver> <owner>
    private boolean handleLeave(CommandSender sender, String leaver, String name) {
        UUID leaverId = getPlayerId(leaver);
        if (leaverId == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return false;
        }

        TeamMySQL.PlayerTeam playerTeam = this.plugin.getTeamCache().getTeam(leaverId);
        if (playerTeam == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotInTeam");
            return false;
        }

        if (!playerTeam.getName().equals(name)) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotInTeam");
            return false;
        }

        if (!this.plugin.getTeamDatabase().leaveTeam(leaverId, playerTeam.getTeamId())) {
            return false;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamLeftMessage");
        return true;
    }

    // /team join <member> <name>
    private boolean handleJoin(CommandSender sender, String member, String name) {
        UUID memberId = getPlayerId(member);
        if (memberId == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return false;
        }

        if (this.plugin.getTeamCache().getTeam(memberId) != null) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamAlreadyInTeam");
            return false;
        }

        int teamId = this.plugin.getTeamDatabase().getTeamId(name);
        if (teamId < 0) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotFound");
            return false;
        }

        if (!this.plugin.getTeamDatabase().joinTeam(memberId, teamId)) {
            return false;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamJoinedMessage");
        return true;
    }

    // /team modify <name> <modifyType> [value]
    // modifyType: name, color
    private boolean handleModify(CommandSender sender, String name, String modifyType, String value) {
        int teamId = this.plugin.getTeamDatabase().getTeamId(name);
        if (teamId < 0) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotFound");
            return false;
        }

        if (!this.plugin.getTeamDatabase().modifyTeam(teamId, modifyType, value)) {
            return false;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamModifiedMessage");
        return true;
    }

    // /team create <name>
    private boolean handleCreate(CommandSender sender, String teamName) {
        int teamId = this.plugin.getTeamDatabase().getTeamId(teamName);
        if (teamId >= 0) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamAlreadyExists");
            return false;
        }

        if (!this.plugin.getTeamDatabase().createTeam(teamName)) {
            return false;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamCreatedMessage", Map.of("team", teamName));
        return true;
    }
}
