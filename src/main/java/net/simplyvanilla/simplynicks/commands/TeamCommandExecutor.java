package net.simplyvanilla.simplynicks.commands;

import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.database.TeamMySQL;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TeamCommandExecutor implements CommandExecutor {
    private final SimplyNicks plugin;

    public TeamCommandExecutor(SimplyNicks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return false;
        }

        String subCommand = args[0];
        switch (subCommand.toLowerCase(Locale.ROOT)) {
            case "create": {
                if (args.length < 3) {
                    return false;
                }
                String player = args[1];
                String teamName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                return handleCreate(sender, player, teamName);
            }
            case "modify": {
                if (args.length < 3) {
                    return false;
                }
                String player = args[1];
                String modifyType = args[2];
                String value = args.length > 3 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : "";
                return handleModify(sender, player, modifyType, value);
            }
            case "join": {
                if (args.length < 3) {
                    return false;
                }
                String member = args[1];
                String owner = args[2];
                return handleJoin(sender, member, owner);
            }
            case "leave": {
                if (args.length < 3) {
                    return false;
                }
                String leaver = args[1];
                String owner = args[2];
                return handleLeave(sender, leaver, owner);
            }
            case "delete":
                if (args.length < 2) {
                    return false;
                }
                String player = args[1];
                return handleDelete(sender, player);
        }

        return false;
    }

    private OfflinePlayer getOfflinePlayer(String input) {
        try {
            UUID uuid = UUID.fromString(input);
            return this.plugin.getServer().getOfflinePlayer(uuid);
        } catch (IllegalArgumentException e) {
            return this.plugin.getServer().getOfflinePlayer(input);
        }
    }

    // /team delete <player>
    private boolean handleDelete(CommandSender sender, String player) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(player);
        if (offlinePlayer.getName() == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return false;
        }

        TeamMySQL.PlayerTeam playerTeam = this.plugin.getTeamCache().getTeam(offlinePlayer.getUniqueId());
        if (playerTeam == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotInTeam");
            return false;
        }

        if (!playerTeam.isOwner()) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotOwner");
            return false;
        }

        if (!this.plugin.getTeamDatabase().deleteTeam(offlinePlayer.getUniqueId())) {
            return false;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamDeletedMessage");
        return true;
    }

    // /team leave <leaver> <owner>
    private boolean handleLeave(CommandSender sender, String leaver, String owner) {
        OfflinePlayer leaverPlayer = getOfflinePlayer(leaver);
        if (leaverPlayer.getName() == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return false;
        }

        if (this.plugin.getTeamCache().getTeam(leaverPlayer.getUniqueId()) == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotInTeam");
            return false;
        }

        OfflinePlayer ownerPlayer = getOfflinePlayer(owner);
        if (ownerPlayer.getName() == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return false;
        }

        if (!this.plugin.getTeamDatabase().leaveTeam(leaverPlayer.getUniqueId(), ownerPlayer.getUniqueId())) {
            return false;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamLeftMessage");
        return true;
    }

    // /team join <member> <owner>
    private boolean handleJoin(CommandSender sender, String member, String owner) {
        OfflinePlayer memberPlayer = getOfflinePlayer(member);
        if (memberPlayer.getName() == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return false;
        }

        if (this.plugin.getTeamCache().getTeam(memberPlayer.getUniqueId()) != null) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamAlreadyInTeam");
            return false;
        }

        OfflinePlayer ownerPlayer = getOfflinePlayer(owner);
        if (ownerPlayer.getName() == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return false;
        }

        TeamMySQL.PlayerTeam playerTeam = this.plugin.getTeamCache().getTeam(ownerPlayer.getUniqueId());
        if (playerTeam == null || !playerTeam.isOwner()) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotFound");
            return false;
        }

        if (!this.plugin.getTeamDatabase().joinTeam(memberPlayer.getUniqueId(), ownerPlayer.getUniqueId())) {
            return false;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamJoinedMessage");
        return true;
    }

    // /team modify <player> <modifyType> [value]
    // modifyType: name, color
    private boolean handleModify(CommandSender sender, String player, String modifyType, String value) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(player);
        if (offlinePlayer.getName() == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return false;
        }

        TeamMySQL.PlayerTeam playerTeam = this.plugin.getTeamCache().getTeam(offlinePlayer.getUniqueId());
        if (playerTeam == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotInTeam");
            return false;
        }

        if (!playerTeam.isOwner()) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamNotOwner");
            return false;
        }

        if (!this.plugin.getTeamDatabase().modifyTeam(offlinePlayer.getUniqueId(), modifyType, value)) {
            return false;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamModifiedMessage");
        return true;
    }

    // /team create <player> <teamName>
    private boolean handleCreate(CommandSender sender, String player, String teamName) {
        OfflinePlayer offlinePlayer = getOfflinePlayer(player);
        if (offlinePlayer.getName() == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return false;
        }

        if (this.plugin.getTeamCache().getTeam(offlinePlayer.getUniqueId()) != null) {
            this.plugin.sendConfigMessage(sender, "messages.error.teamAlreadyInTeam");
            return false;
        }

        if (!this.plugin.getTeamDatabase().createTeam(offlinePlayer.getUniqueId(), teamName)) {
            return false;
        }

        this.plugin.sendConfigMessage(sender, "messages.teamCreatedMessage", Map.of("team", teamName));
        return true;
    }
}
