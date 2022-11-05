package net.simplyvanilla.simplynicks.commands;

import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.util.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class NickCommandExecutor implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Map<String, String> messageReplacements = new HashMap<>();

        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player) sender;

            if (!GamePermissionUtil.hasPermission(sender, "simplynicks.nick")) {
                MessageUtil.sendMessage(sender, "messages.error.permissionErrorMessage");
                return true;
            }

            if (args[0].equals("reset") || player.getName().equals(args[0])) {
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

            if (!SimplyNicks.getCache().isNickAvailable(args[0], player.getName())) {
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

            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

            if (player.getName() == null) {
                MessageUtil.sendMessage(sender, "messages.error.playerCannotFoundErrorMessage");
                return true;
            }

            if (args[1].equals("reset") || player.getName().equals(args[1])) {
                resetNick((Player) player);

                if (player.isOnline()) {
                    MessageUtil.sendMessage(player.getPlayer(), "messages.nickResetMessageByModerator");
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

            if (!SimplyNicks.getCache().isNickAvailable(args[1], player.getName())) {
                MessageUtil.sendMessage(sender, "messages.error.nickAlreadyInUseMessage");
                return true;
            }

            if (!setNick((Player) player, args[1])) {
                return true;
            }

            if (player.isOnline()) {
                MessageUtil.sendMessage(player.getPlayer(), "messages.nickChangedByModeratorMessage", messageReplacements);
            }

            MessageUtil.sendMessage(sender, "messages.moderatorNickChangedMessage", messageReplacements);
        }

        return true;
    }

    private static boolean setNick(Player player, String nick) {
        if (!SimplyNicks.getDatabase().updatePlayerNickData(player.getUniqueId(), nick)) {
            return false;
        }

        if (player.isOnline()) {
            NickUtil.applyNick(player, nick);
        }

        return true;
    }

    private static void resetNick(Player player) {
        SimplyNicks.getDatabase().removePlayerNickData(player.getUniqueId());
        if (player.isOnline()) {
            player.displayName(null);
        }
    }
}
