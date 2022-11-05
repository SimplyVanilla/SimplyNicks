package net.simplyvanilla.simplynicks.commands;

import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.util.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NickCommandExecutor implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Map<String, String> messageReplacements = new HashMap<>();

        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player) sender;

            if (!GamePermissionUtil.hasPermission(sender, "simplynicks.changenick")) {
                MessageUtil.sendMessage(sender, "messages.error.permissionErrorMessage");
                return true;
            }

            if (args[0].equals("reset")) {
                resetNick(player);
                MessageUtil.sendMessage(sender, "messages.nickResetMessage");
                return true;
            }

            messageReplacements.put("nick", args[0]);

            if (!NickValidationUtil.isValidNick(args[0])) {
                MessageUtil.sendMessage(sender, "messages.error.nickValidationErrorMessage");
                return true;
            }

            if (!GamePermissionUtil.hasColorPermission(
                sender, NickValidationUtil.getColorGroup(args[0], SimplyNicks.colors))) {
                MessageUtil.sendMessage(sender, "messages.error.colorPermissionErrorMessage");
                return true;
            }

            if (!SimplyNicks.getCache().isNickAvailable(args[0])) {
                MessageUtil.sendMessage(sender, "messages.error.nickAlreadyInUseMessage");
                return true;
            }

            if (setNick(player, args[0])) {
                MessageUtil.sendMessage(sender, "messages.nickChangedSuccessfullyMessage", messageReplacements);
            }

        } else if (args.length == 2) {
            if (!GamePermissionUtil.hasPermission(sender, "simplynicks.changeothers")) {
                MessageUtil.sendMessage(sender, "messages.error.permissionErrorMessage");
                return true;
            }

            Player player = Bukkit.getPlayer(args[0]);
            UUID uuid = null;
            if (player == null) {
                uuid = PlayerUtil.tryUUID(args[0]);
            }

            if (player == null && uuid == null) {
                MessageUtil.sendMessage(sender, "messages.error.playerCannotFoundErrorMessage");
                return true;
            }

            if (args[1].equals("reset")) {
                if (player != null) {
                    resetNick(player);
                    MessageUtil.sendMessage(player, "messages.nickResetMessageByModerator");
                } else {
                    resetNick(uuid);
                }
                MessageUtil.sendMessage(sender, "messages.moderatorNickResetMessage");
                return true;
            }

            messageReplacements.put("nick", args[1]);

            if (!NickValidationUtil.isValidNick(args[1])) {
                MessageUtil.sendMessage(sender, "messages.error.nickValidationErrorMessage");
                return true;
            }

            if (!GamePermissionUtil.hasColorPermission(
                sender, NickValidationUtil.getColorGroup(args[1], SimplyNicks.colors))) {
                MessageUtil.sendMessage(sender, "messages.error.colorPermissionErrorMessage");
                return true;
            }

            if (!SimplyNicks.getCache().isNickAvailable(args[1])) {
                MessageUtil.sendMessage(sender, "messages.error.nickAlreadyInUseMessage");
                return true;
            }

            if (player != null && setNick(player, args[1])) {
                MessageUtil.sendMessage(player, "messages.nickChangedByModeratorMessage", messageReplacements);
            } else if (player == null && !setNick(uuid, args[1])) {
                return true;
            } else {
                return true;
            }

            MessageUtil.sendMessage(sender, "messages.moderatorNickChangedMessage", messageReplacements);
        }

        return true;
    }

    private static boolean setNick(Player player, String nick) {
        if (!setNick(player.getUniqueId(), nick)) {
            return false;
        }

        NickUtil.applyNick(player, nick);
        return true;
    }

    private static boolean setNick(UUID uuid, String nick) {
        return SimplyNicks.getDatabase().updatePlayerNickData(uuid, nick);
    }

    private static void resetNick(Player player) {
        resetNick(player.getUniqueId());
        player.displayName(null);
    }

    private static void resetNick(UUID uuid) {
        SimplyNicks.getDatabase().removePlayerNickData(uuid);
    }
}
