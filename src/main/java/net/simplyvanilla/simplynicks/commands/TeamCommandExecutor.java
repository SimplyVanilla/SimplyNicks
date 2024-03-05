package net.simplyvanilla.simplynicks.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

public class TeamCommandExecutor implements CommandExecutor {
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

    // /team delete <player>
    private boolean handleDelete(CommandSender sender, String player) {
        return false; // TODO
    }

    // /team leave <leaver> <owner>
    private boolean handleLeave(CommandSender sender, String leaver, String owner) {
        return false; // TODO
    }

    // /team join <member> <owner>
    private boolean handleJoin(CommandSender sender, String member, String owner) {
        return false; // TODO
    }

    // /team modify <player> <modifyType> [value]
    // modifyType: name, color
    private boolean handleModify(CommandSender sender, String player, String modifyType, String value) {
        return false; // TODO
    }

    // /team create <player> <teamName>
    private boolean handleCreate(CommandSender sender, String player, String teamName) {
        return false; // TODO
    }
}
