package net.simplyvanilla.simplynicks.database;

import net.simplyvanilla.simplynicks.SimplyNicks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;

public class Cache {
    private Map<String, String> nicks;

    public void initCache() {
        this.nicks = SimplyNicks.getDatabase().getAllNames();
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

    public boolean isNickAvailable(String checkNick) {
        if (this.nicks.containsValue(checkNick)) {
            return false;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equals(checkNick)) {
                return false;
            }
        }

        return true;
    }

    public String getUUIDByName(String name) {

        for (var entry : this.nicks.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }

        return null;
    }
}
