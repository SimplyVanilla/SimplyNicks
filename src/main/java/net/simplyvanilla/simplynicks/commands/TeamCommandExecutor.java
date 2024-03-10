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

        String subCommand = args[0].toLowerCase(Locale.ROOT);
        switch (subCommand) {
            case "remove":
            case "add": {
                if (args.length < 2) {
                    return false;
                }
                if (subCommand.equals("remove")) {
                    handleDelete(sender, args[1]);
                } else {
                    handleCreate(sender, args[1]);
                }
                return true;
            }
            case "modify": {
                if (args.length < 3) {
                    return false;
                }
                String name = args[1];
                String modifyType = args[2];
                String value = args.length > 3 ? args[3] : "";
                handleModify(sender, name, modifyType, value);
                return true;
            }
            case "leave":
            case "join": {
                if (args.length < 3) {
                    return false;
                }
                String name = args[1];
                String member = args[2];
                if (subCommand.equals("join")) {
                    handleJoin(sender, member, name);
                } else {
                    handleLeave(sender, member, name);
                }
                return true;
            }
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
    private void handleDelete(CommandSender sender, String name) {
        int teamId = this.plugin.getTeamDatabase().getTeamId(name);
        if (teamId < 0) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotFound");
            return;
        }

        if (!this.plugin.getTeamDatabase().deleteTeam(teamId)) {
            return;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamDeletedMessage");
    }

    // /team leave <leaver> <owner>
    private void handleLeave(CommandSender sender, String leaver, String name) {
        UUID leaverId = getPlayerId(leaver);
        if (leaverId == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return;
        }

        TeamMySQL.PlayerTeam playerTeam = this.plugin.getTeamCache().getTeam(leaverId);
        if (playerTeam == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotInTeam");
            return;
        }

        if (!playerTeam.getName().equals(name)) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotInTeam");
            return;
        }

        if (!this.plugin.getTeamDatabase().leaveTeam(leaverId, playerTeam.getTeamId())) {
            return;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamLeftMessage");
    }

    // /team join <member> <name>
    private void handleJoin(CommandSender sender, String member, String name) {
        UUID memberId = getPlayerId(member);
        if (memberId == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return;
        }

        if (this.plugin.getTeamCache().getTeam(memberId) != null) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamAlreadyInTeam");
            return;
        }

        int teamId = this.plugin.getTeamDatabase().getTeamId(name);
        if (teamId < 0) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotFound");
            return;
        }

        if (!this.plugin.getTeamDatabase().joinTeam(memberId, teamId)) {
            return;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamJoinedMessage");
    }

    // /team modify <name> <modifyType> [value]
    // modifyType: name, color
    private void handleModify(CommandSender sender, String name, String modifyType, String value) {
        int teamId = this.plugin.getTeamDatabase().getTeamId(name);
        if (teamId < 0) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotFound");
            return;
        }

        if (!this.plugin.getTeamDatabase().modifyTeam(teamId, modifyType, value)) {
            return;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamModifiedMessage");
    }

    // /team create <name>
    private void handleCreate(CommandSender sender, String teamName) {
        int teamId = this.plugin.getTeamDatabase().getTeamId(teamName);
        if (teamId >= 0) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamAlreadyExists");
            return;
        }

        if (!this.plugin.getTeamDatabase().createTeam(teamName)) {
            return;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamCreatedMessage", Map.of("team", teamName));
    }
}
