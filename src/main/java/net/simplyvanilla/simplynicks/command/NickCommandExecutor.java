package net.simplyvanilla.simplynicks.command;

import java.util.Objects;

import me.clip.placeholderapi.PlaceholderAPI;
import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.util.GamePermissionUtil;
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
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.permissionErrorMessage")));
                return true;
            }

            if (args[0].equals("reset")) {
                SimplyNicks.getDatabase().removePlayerNameData(player);
                player.setDisplayName(null);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(this.config.getString("messages.nickResettedMessage")))));
                return true;
            }

            if (!NickValidationUtil.isValidNick(args[0], this.config.getBoolean("countColorCodesAsCharacter"))) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.nickValidationErrorMessage")));
                return true;
            }

            if (!GamePermissionUtil.hasColorPermission(
                sender, NickValidationUtil.getColorGroup(args[0], this.config.getStringList("colors")))) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.colorPermissionErrorMessage")));
                return true;
            }

            if (!SimplyNicks.getCache().isNameAvailable(args[0])) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.nameAlreadyInUseMessage")));
                return true;
            }

            SimplyNicks.getDatabase().updatePlayerNameData(player, args[0]);
            player.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[0]));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(this.config.getString("messages.nickChangedSuccessfullyMessage")))));

        } else if (args.length == 2) {
            if (!GamePermissionUtil.hasPermission(sender, "simplynicks.changeothers")) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.permissionErrorMessage")));
                return true;
            }

            player = Bukkit.getPlayer(args[0]);

            if (args[1].equals("reset")) {
                SimplyNicks.getDatabase().removePlayerNameData(player);
                player.setDisplayName(null);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(this.config.getString("messages.nickResettedMessageByModerator")))));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(this.config.getString("messages.moderatorNickResetMessage")))));
                return true;
            }

            if (player == null) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.playerCannotFoundErrorMessage")));
                return true;
            }

            if (!NickValidationUtil.isValidNick(args[1], this.config.getBoolean("countColorCodesAsCharacter"))) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.nickValidationErrorMessage")));
                return true;
            }

            if (!GamePermissionUtil.hasColorPermission(
                sender, NickValidationUtil.getColorGroup(args[1], this.config.getStringList("colors")))) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.colorPermissionErrorMessage")));
                return true;
            }

            if (!SimplyNicks.getCache().isNameAvailable(args[1])) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.nameAlreadyInUseMessage")));
                return true;
            }

            SimplyNicks.getDatabase().updatePlayerNameData(player, args[1]);
            player.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[1]));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(this.config.getString("messages.nickChangedByModeratorMessage")))));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(this.config.getString("messages.moderatorNickChangedMessage")))));
        }

        return true;
    }
}
