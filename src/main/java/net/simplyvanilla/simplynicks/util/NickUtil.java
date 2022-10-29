package net.simplyvanilla.simplynicks.util;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class NickUtil {
    public static void applyNick(Player player, String nick) {
        // ensure nick does not overflow
        nick = nick.replaceAll("(&r)+$", "");
        nick += "&r";

        player.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(nick));
    }
}
