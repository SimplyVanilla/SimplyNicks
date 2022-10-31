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
        SimplyNicks.getInstance().getLogger().info(this.nicks.toString());

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

    public UUID getUUIDByNick(String nick) {
        SimplyNicks.getInstance().getLogger().info(this.nicks.toString());

        for (var entry : this.nicks.entrySet()) {
            if (entry.getValue().equals(nick)) {
                return UUID.fromString(entry.getKey());
            }
        }

        return null;
    }
}
