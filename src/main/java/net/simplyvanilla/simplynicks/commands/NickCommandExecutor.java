package net.simplyvanilla.simplynicks.commands;

import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.util.GamePermissionUtil;
import net.simplyvanilla.simplynicks.util.MessageUtil;
import net.simplyvanilla.simplynicks.util.NickUtil;
import net.simplyvanilla.simplynicks.util.NickValidationUtil;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        Map<String, String> messageReplacements = new HashMap<>();

        if (args.length == 1 && sender instanceof Player player) {

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
                sender, NickValidationUtil.getColorGroup(args[0], SimplyNicks.getInstance().getColors()))) {
                MessageUtil.sendMessage(sender, "messages.error.colorPermissionErrorMessage");
                return true;
            }

            if (!SimplyNicks.getInstance().getCache().isNickAvailable(args[0], player.getName())) {
                MessageUtil.sendMessage(sender, "messages.error.nickAlreadyInUseMessage");
                return true;
            }

            if (setNick(player, args[0])) {
                MessageUtil.sendMessage(sender, "messages.nickChangedSuccessfullyMessage",
                    messageReplacements);
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
                if (player.isOnline()) {
                    resetNick(Objects.requireNonNull(player.getPlayer()));
                    MessageUtil.sendMessage(player.getPlayer(),
                        "messages.nickResetMessageByModerator");
                } else {
                    resetNick(player.getUniqueId());
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
                sender, NickValidationUtil.getColorGroup(args[1], SimplyNicks.getInstance().getColors()))) {
                MessageUtil.sendMessage(sender, "messages.error.colorPermissionErrorMessage");
                return true;
            }

            if (!SimplyNicks.getInstance().getCache().isNickAvailable(args[1], player.getName())) {
                MessageUtil.sendMessage(sender, "messages.error.nickAlreadyInUseMessage");
                return true;
            }

            if (player.isOnline()) {
                if (!setNick(Objects.requireNonNull(player.getPlayer()), args[1])) {
                    return true;
                }
                MessageUtil.sendMessage(player.getPlayer(),
                    "messages.nickChangedByModeratorMessage", messageReplacements);
            } else {
                if (!setNick(player.getUniqueId(), args[1])) {
                    return true;
                }
            }

            MessageUtil.sendMessage(sender, "messages.moderatorNickChangedMessage",
                messageReplacements);
        } else {
            return false;
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
        return SimplyNicks.getInstance().getDatabase().updatePlayerNickData(uuid, nick);
    }

    private static void resetNick(Player player) {
        resetNick(player.getUniqueId());
        player.displayName(null);
    }

    private static void resetNick(UUID uuid) {
        SimplyNicks.getInstance().getDatabase().removePlayerNickData(uuid);
    }
}
