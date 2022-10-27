// 
// Decompiled by Procyon v0.5.36
// 

package me.orbitium.command;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import me.orbitium.SimplyNicks;
import org.bukkit.configuration.Configuration;
import org.bukkit.command.CommandExecutor;

public class NameCommandExecutor implements CommandExecutor
{
    Configuration config;
    
    public NameCommandExecutor() {
        this.config = (Configuration)SimplyNicks.getInstance().getConfig();
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            final Player pSender = (Player)sender;
            this.isColorPermissionsValid(sender, args[0]);
            if (sender.hasPermission(this.config.getString("permissions.changeOwnName")) || (sender.isOp() && this.config.getBoolean("OPColorPermissionCheck"))) {
                if (!SimplyNicks.getCache().isNameAvailable(args[0])) {
                    sender.sendMessage(this.config.getString("messages.error.nameAlreadyInUseMessage"));
                    return true;
                }
                if (!this.isColorPermissionsValid(sender, args[0])) {
                    sender.sendMessage(this.config.getString("messages.error.colorPermissionErrorMessage"));
                    return true;
                }
                if (!this.isNameLenghtValid(sender, args[0])) {
                    return true;
                }
                SimplyNicks.getDatabase().updatePlayerNameData(pSender, args[0]);
                pSender.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[0]));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(pSender, this.config.getString("messages.nickChangedSuccessfullyMessage"))));
            }
            else {
                sender.sendMessage(this.config.getString("messages.error.permissionErrorMessage"));
            }
        }
        else if (args.length == 2) {
            if (!sender.hasPermission(this.config.getString("permissions.changeOthersName")) || (sender.isOp() && this.config.getBoolean("OPColorPermissionCheck"))) {
                sender.sendMessage(this.config.getString("messages.error.permissionErrorMessage"));
                return true;
            }
            final Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage(this.config.getString("messages.error.playerCannotFoundErrorMessage"));
                return true;
            }
            if (!SimplyNicks.getCache().isNameAvailable(args[1])) {
                sender.sendMessage(this.config.getString("messages.error.nameAlreadyInUseMessage"));
                return true;
            }
            if (!this.isColorPermissionsValid(sender, args[1])) {
                sender.sendMessage(this.config.getString("messages.error.colorPermissionErrorMessage"));
                return true;
            }
            if (!this.isNameLenghtValid(sender, args[1])) {
                return true;
            }
            SimplyNicks.getDatabase().updatePlayerNameData(player, args[1]);
            player.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[1]));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, this.config.getString("messages.nickChangedByModeratorMessage"))));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, this.config.getString("messages.moderatorNickChangedMessage"))));
        }
        return true;
    }
    
    boolean isColorPermissionsValid(final CommandSender sender, final String name) {
        boolean valid = true;
        boolean set = false;
        final List<String> colors = new ArrayList<String>();
        for (final char c : name.toCharArray()) {
            if (c == '&') {
                set = true;
            }
            else if (set) {
                set = false;
                colors.add("&" + c);
            }
        }
        for (final String color : colors) {
            if (!SimplyNicks.colors.contains(color)) {
                return sender.isOp() && !this.config.getBoolean("OPColorPermissionCheck");
            }
            if (sender.isPermissionSet(this.config.getString("permissions.accessColors"))) {
                continue;
            }
            valid = false;
        }
        return (sender.isOp() && !this.config.getBoolean("OPColorPermissionCheck")) || valid;
    }
    
    boolean isNameLenghtValid(final CommandSender sender, String name) {
        if (!this.config.getBoolean("countColorCodesAsCharacter")) {
            name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
        }
        if (name.length() < 3) {
            sender.sendMessage(this.config.getString("messages.error.shortNickErrorMessage"));
            return false;
        }
        if (name.length() > 16) {
            sender.sendMessage(this.config.getString("messages.error.longNickErrorMessage"));
            return false;
        }
        return true;
    }
}
