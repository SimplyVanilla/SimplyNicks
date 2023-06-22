package net.simplyvanilla.simplynicks.commands;

import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.util.GamePermissionUtil;
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
    private final SimplyNicks plugin;

    public NickCommandExecutor(SimplyNicks plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        Map<String, String> messageReplacements = new HashMap<>();

        if (args.length == 1 && sender instanceof Player player) {
            handleSingleArgument(sender, player, args[0], messageReplacements);
        } else if (args.length == 2) {
            handleTwoArguments(sender, args, messageReplacements);
        }
        return true;
    }

    private void handleSingleArgument(CommandSender sender, Player player, String arg,
                                      Map<String, String> messageReplacements) {
        if (!hasNickPermission(sender)) {
            return;
        }

        if (arg.equals("reset") || player.getName().equals(arg)) {
            resetNick(player);
            this.plugin.sendConfigMessage(sender, "messages.nickResetMessage");
            return;
        }

        messageReplacements.put("nick", arg);

        if (!validateNickAndColor(sender, arg)) {
            return;
        }

        if (!isNickAvailable(sender, arg, player.getName())) {
            return;
        }

        if (setNick(player, arg)) {
            this.plugin.sendConfigMessage(sender, "messages.nickChangedSuccessfullyMessage",
                messageReplacements);
        }
    }

    private void handleTwoArguments(CommandSender sender, String[] args,
                                    Map<String, String> messageReplacements) {
        if (!hasChangeOthersPermission(sender)) {
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (player.getName() == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return;
        }

        if (args[1].equals("reset") || player.getName().equals(args[1])) {
            handleResetNick(sender, player);
            return;
        }

        messageReplacements.put("nick", args[1]);

        if (!validateNickAndColor(sender, args[1])) {
            return;
        }

        if (!isNickAvailable(sender, args[1], player.getName())) {
            return;
        }

        handleSetNick(sender, player, args[1], messageReplacements);
    }

    private boolean hasNickPermission(CommandSender sender) {
        if (!GamePermissionUtil.hasPermission(sender, "simplynicks.nick")) {
            this.plugin.sendConfigMessage(sender, "messages.error.permissionErrorMessage");
            return false;
        }
        return true;
    }

    private boolean hasChangeOthersPermission(CommandSender sender) {
        if (!GamePermissionUtil.hasPermission(sender, "simplynicks.changeothers")) {
            this.plugin.sendConfigMessage(sender, "messages.error.permissionErrorMessage");
            return false;
        }
        return true;
    }

    private boolean validateNickAndColor(CommandSender sender, String nick) {
        if (!NickValidationUtil.isValidNick(nick)) {
            this.plugin.sendConfigMessage(sender, "messages.error.nickValidationErrorMessage");
            return false;
        }
        if (!GamePermissionUtil.hasColorPermission(
            sender, NickValidationUtil.getColorGroup(nick, this.plugin.getColors()))) {
            this.plugin.sendConfigMessage(sender, "messages.error.colorPermissionErrorMessage");
            return false;
        }
        return true;
    }

    private boolean isNickAvailable(CommandSender sender, String nick, String name) {
        if (!this.plugin.getCache().isNickAvailable(nick, name)) {
            this.plugin.sendConfigMessage(sender, "messages.error.nickAlreadyInUseMessage");
            return false;
        }
        return true;
    }

    private boolean handleResetNick(CommandSender sender, OfflinePlayer player) {
        if (player.isOnline()) {
            resetNick(Objects.requireNonNull(player.getPlayer()));
            this.plugin.sendConfigMessage(player.getPlayer(),
                "messages.nickResetMessageByModerator");
        } else {
            resetNick(player.getUniqueId());
        }
        this.plugin.sendConfigMessage(sender, "messages.moderatorNickResetMessage");
        return true;
    }

    private void handleSetNick(CommandSender sender, OfflinePlayer player, String nick,
                               Map<String, String> messageReplacements) {
        if (player.isOnline()) {
            if (!setNick(Objects.requireNonNull(player.getPlayer()), nick)) {
                return;
            }
            this.plugin.sendConfigMessage(player.getPlayer(),
                "messages.nickChangedByModeratorMessage",
                messageReplacements);
        } else {
            if (!setNick(player.getUniqueId(), nick)) {
                return;
            }
        }
        this.plugin.sendConfigMessage(sender, "messages.moderatorNickChangedMessage",
            messageReplacements);
    }

    private boolean setNick(Player player, String nick) {
        if (!setNick(player.getUniqueId(), nick)) {
            return false;
        }
        NickUtil.applyNick(player, nick);
        return true;
    }

    private boolean setNick(UUID uuid, String nick) {
        return this.plugin.getDatabase().updatePlayerNickData(uuid, nick);
    }

    private void resetNick(Player player) {
        resetNick(player.getUniqueId());
        player.displayName(null);
    }

    private void resetNick(UUID uuid) {
        this.plugin.getDatabase().removePlayerNickData(uuid);
    }
}
