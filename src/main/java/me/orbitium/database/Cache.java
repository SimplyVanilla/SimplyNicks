package me.orbitium.database;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import me.orbitium.SimplyNicks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Cache {
    Map<String, String> names;

    public void initCache() {
        this.names = SimplyNicks.getDatabase().getAllNames();
    }

    public void addNewName(String uuid, String name) {
        this.names.put(uuid, name);
    }

    public void removeName(String uuid) {
        this.names.remove(uuid);
    }

    public String getName(String playerUUID) {
        return (String)this.names.get(playerUUID);
    }

    public boolean isNameAvailable(String name) {
        name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
        Iterator var2 = this.names.entrySet().iterator();

        Entry entry;
        do {
            if (!var2.hasNext()) {
                var2 = Bukkit.getOnlinePlayers().iterator();

                Player onlinePlayer;
                do {
                    if (!var2.hasNext()) {
                        return true;
                    }

                    onlinePlayer = (Player)var2.next();
                } while(!onlinePlayer.getName().equals(name));

                return false;
            }

            entry = (Entry)var2.next();
        } while(!ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String)entry.getValue())).equals(name));

        return false;
    }

    public boolean isNameAvailable(String UUID, String name) {
        name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
        Iterator var3 = this.names.entrySet().iterator();

        Entry entry;
        do {
            if (!var3.hasNext()) {
                var3 = Bukkit.getOnlinePlayers().iterator();

                Player onlinePlayer;
                do {
                    if (!var3.hasNext()) {
                        return true;
                    }

                    onlinePlayer = (Player)var3.next();
                } while(!onlinePlayer.getName().equals(name) || onlinePlayer.getUniqueId().toString().equals(UUID));

                return false;
            }

            entry = (Entry)var3.next();
        } while(!ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String)entry.getValue())).equals(name) || ((String)entry.getKey()).equals(UUID));

        return false;
    }

    public String getUUIDByName(String name) {
        Iterator var2 = this.names.entrySet().iterator();

        Entry entry;
        do {
            if (!var2.hasNext()) {
                throw new NullPointerException("There is no UUID about " + name);
            }

            entry = (Entry)var2.next();
        } while(!ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String)entry.getValue())).equals(name));

        return (String)entry.getKey();
    }
}