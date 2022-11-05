package net.simplyvanilla.simplynicks.util;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.permissions.Permissible;

public class GamePermissionUtil {

    public static boolean hasPermission(Permissible sender, String permission) {
        return sender instanceof ConsoleCommandSender ||
                sender instanceof RemoteConsoleCommandSender ||
                sender.hasPermission(permission);
    }

    public static boolean hasColorPermission(Permissible sender, NickValidationUtil.ColorGroup colorGroup) {
        if (colorGroup == NickValidationUtil.ColorGroup.ALL && hasPermission(sender, "simplynicks.colors.all")) {
            return true;
        } else if (colorGroup == NickValidationUtil.ColorGroup.DEFAULT && (
            hasPermission(sender, "simplynicks.colors.default") ||
                hasPermission(sender, "simplynicks.colors.all")
        )) {
            return true;
        } else return colorGroup == NickValidationUtil.ColorGroup.NONE;
    }
}
