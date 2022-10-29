package net.simplyvanilla.simplynicks.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class NickUtil {
    public static void applyNick(Player player, String nick) {
        // ensure nick does not overflow
        nick = nick.replaceAll("(&r)+$", "");
        nick += "&r";

        player.setDisplayName(ChatColor.translateAlternateColorCodes('&', nick));
    }
}
