package net.simplyvanilla.simplynicks.command;

import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.util.GamePermissionUtil;
import net.simplyvanilla.simplynicks.util.MessageUtil;
import net.simplyvanilla.simplynicks.util.NickValidationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NickCommandExecutor implements CommandExecutor {
    Configuration config = SimplyNicks.getInstance().getConfig();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player player;

        if (args.length == 1 && sender instanceof Player) {
            player = (Player)sender;

            if (!GamePermissionUtil.hasPermission(sender, "simplynicks.changenick")) {
                MessageUtil.sendMessage(sender, "messages.error.permissionErrorMessage");
                return true;
            }

            if (args[0].equals("reset")) {
                SimplyNicks.getDatabase().removePlayerNameData(player);
                player.setDisplayName(null);
                MessageUtil.sendMessage(sender, "messages.nickResetMessage");
                return true;
            }

            if (!NickValidationUtil.isValidNick(args[0])) {
                MessageUtil.sendMessage(sender, "messages.error.nickValidationErrorMessage");
                return true;
            }

            SimplyNicks.getInstance().getLogger().info(NickValidationUtil.getColorGroup(args[0], this.config.getStringList("colors")).toString());

            if (!GamePermissionUtil.hasColorPermission(
                sender, NickValidationUtil.getColorGroup(args[0], this.config.getStringList("colors")))) {
                MessageUtil.sendMessage(sender, "messages.error.colorPermissionErrorMessage");
                return true;
            }

            if (!SimplyNicks.getCache().isNameAvailable(args[0])) {
                MessageUtil.sendMessage(sender, "messages.error.nameAlreadyInUseMessage");
                return true;
            }

            setNick(player, args[0]);
            MessageUtil.sendMessage(sender, "messages.nickChangedSuccessfullyMessage", args[0]);

        } else if (args.length == 2) {
            if (!GamePermissionUtil.hasPermission(sender, "simplynicks.changeothers")) {
                MessageUtil.sendMessage(sender, "messages.error.permissionErrorMessage");
                return true;
            }

            player = Bukkit.getPlayer(args[0]);

            if (args[1].equals("reset")) {
                SimplyNicks.getDatabase().removePlayerNameData(player);
                player.setDisplayName(null);
                MessageUtil.sendMessage(player, "messages.nickResetMessageByModerator");
                MessageUtil.sendMessage(sender, "messages.moderatorNickResetMessage");
                return true;
            }

            if (player == null) {
                MessageUtil.sendMessage(sender, "messages.error.playerCannotFoundErrorMessage");
                return true;
            }

            if (!NickValidationUtil.isValidNick(args[1])) {
                MessageUtil.sendMessage(sender, "messages.error.nickValidationErrorMessage");
                return true;
            }

            if (!GamePermissionUtil.hasColorPermission(
                sender, NickValidationUtil.getColorGroup(args[1], this.config.getStringList("colors")))) {
                MessageUtil.sendMessage(sender, "messages.error.colorPermissionErrorMessage");
                return true;
            }

            if (!SimplyNicks.getCache().isNameAvailable(args[1])) {
                MessageUtil.sendMessage(sender, "messages.error.nameAlreadyInUseMessage");
                return true;
            }

            setNick(player, args[1]);
            MessageUtil.sendMessage(player, "messages.nickChangedByModeratorMessage", args[1]);
            MessageUtil.sendMessage(sender, "messages.moderatorNickChangedMessage", args[1]);
        }

        return true;
    }

    private static void setNick(Player player, String nick) {
        nick = nick.replaceAll("(&r)+$", "");
        nick += "&r";
        SimplyNicks.getDatabase().updatePlayerNameData(player, nick);
        player.setDisplayName(ChatColor.translateAlternateColorCodes('&', nick));
    }
}
