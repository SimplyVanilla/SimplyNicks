package net.simplyvanilla.simplynicks.database;

import net.simplyvanilla.simplynicks.SimplyNicks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cache {
    private Map<String, String> nicks = new HashMap<>();

    public void initCache() {
        for (var entry : SimplyNicks.getDatabase().getAllNicks().entrySet()) {
            addNick(entry.getKey(), entry.getValue());
        }
    }

    public String getNick(String uuid) {
        return this.nicks.get(uuid);
    }

    public void addNick(String uuid, String nick) {
        this.nicks.put(
            uuid,
            ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', nick))
        );
    }

    public void removeNick(String uuid) {
        this.nicks.remove(uuid);
    }

    public boolean isNickAvailable(String nick, String currentName) {
        nick = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', nick));
        if (nick.equals(currentName)) {
            return true;
        }

        return isNickAvailable(nick);
    }

    public boolean isNickAvailable(String nick) {
        nick = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', nick));

        if (this.nicks.containsValue(nick)) {
            return false;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equals(nick)) {
                return false;
            }
        }

        return true;
    }

    public UUID getUUIDByNick(String nick) {
        for (var entry : this.nicks.entrySet()) {
            if (entry.getValue().equals(nick)) {
                return UUID.fromString(entry.getKey());
            }
        }

        return null;
    }
}
