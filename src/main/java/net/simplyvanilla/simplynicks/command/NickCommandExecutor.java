package net.simplyvanilla.simplynicks.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.clip.placeholderapi.PlaceholderAPI;
import net.simplyvanilla.simplynicks.SimplyNicks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

public class NickCommandExecutor implements CommandExecutor {
    Configuration config = SimplyNicks.getInstance().getConfig();

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;
        if (args.length == 1 && sender instanceof Player) {
            player = (Player)sender;
            if (!sender.hasPermission(Objects.requireNonNull(this.config.getString("permissions.changeOwnName"))) && (!sender.isOp() || !this.config.getBoolean("OPColorPermissionCheck"))) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.permissionErrorMessage")));
            } else {
                if (args[0].equals("reset")) {
                    SimplyNicks.getDatabase().updatePlayerNameData(player, player.getName());
                    player.setDisplayName(player.getName());
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(this.config.getString("messages.nickResettedMessage")))));
                    return true;
                }

                this.isColorPermissionsValid(sender, args[0]);
                if (!SimplyNicks.getCache().isNameAvailable(args[0])) {
                    sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.nameAlreadyInUseMessage")));
                    return true;
                }

                if (!this.isColorPermissionsValid(sender, args[0])) {
                    sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.colorPermissionErrorMessage")));
                    return true;
                }

                if (!this.isNameLenghtValid(sender, args[0])) {
                    return true;
                }

                SimplyNicks.getDatabase().updatePlayerNameData(player, args[0]);
                player.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[0]));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(this.config.getString("messages.nickChangedSuccessfullyMessage")))));
            }
        } else if (args.length == 2) {
            if (!sender.hasPermission(Objects.requireNonNull(this.config.getString("permissions.changeOthersName"))) || sender.isOp() && this.config.getBoolean("OPColorPermissionCheck")) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.permissionErrorMessage")));
                return true;
            }

            player = Bukkit.getPlayer(args[0]);

            if (args[1].equals("reset")) {
                SimplyNicks.getDatabase().updatePlayerNameData(player, player.getName());
                player.setDisplayName(player.getName());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(this.config.getString("messages.nickResettedMessageByModerator")))));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(this.config.getString("messages.moderatorNickResetMessage")))));
                return true;
            }

            if (player == null) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.playerCannotFoundErrorMessage")));
                return true;
            }

            if (!SimplyNicks.getCache().isNameAvailable(args[1])) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.nameAlreadyInUseMessage")));
                return true;
            }

            if (!this.isColorPermissionsValid(sender, args[1])) {
                sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.colorPermissionErrorMessage")));
                return true;
            }

            if (!this.isNameLenghtValid(sender, args[1])) {
                return true;
            }

            SimplyNicks.getDatabase().updatePlayerNameData(player, args[1]);
            player.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[1]));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(this.config.getString("messages.nickChangedByModeratorMessage")))));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(this.config.getString("messages.moderatorNickChangedMessage")))));
        }

        return true;
    }

    boolean isColorPermissionsValid(CommandSender sender, String name) {
        boolean valid = true;
        boolean set = false;
        List<String> colors = new ArrayList<>();
        char[] var6 = name.toCharArray();

        for (char c : var6) {
            if (c == '&') {
                set = true;
            } else if (set) {
                set = false;
                colors.add("&" + c);
            }
        }

        for (String color : colors) {
            if (!SimplyNicks.colors.contains(color)) {
                return sender.isOp() && !this.config.getBoolean("OPColorPermissionCheck");
            }

            if (!sender.isPermissionSet(Objects.requireNonNull(this.config.getString("permissions.accessColors")))) {
                valid = false;
            }
        }

        return sender.isOp() && !this.config.getBoolean("OPColorPermissionCheck") || valid;
    }

    boolean isNameLenghtValid(CommandSender sender, String name) {
        if (!this.config.getBoolean("countColorCodesAsCharacter")) {
            name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
        }

        if (name.length() < 3) {
            sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.shortNickErrorMessage")));
            return false;
        } else if (name.length() > 16) {
            sender.sendMessage(Objects.requireNonNull(this.config.getString("messages.error.longNickErrorMessage")));
            return false;
        } else {
            return true;
        }
    }
}
