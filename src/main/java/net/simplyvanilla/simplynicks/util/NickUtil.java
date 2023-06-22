package net.simplyvanilla.simplynicks.util;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class NickUtil {
    private NickUtil() {
    }

    public static void applyNick(Player player, String nick) {
        player.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(nick));
    }
}
