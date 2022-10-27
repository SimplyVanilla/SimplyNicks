// 
// Decompiled by Procyon v0.5.36
// 

package me.orbitium.database;

import java.util.Set;
import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import me.orbitium.SimplyNicks;
import java.util.Map;

public class Cache
{
    Map<String, String> names;
    
    public void initCache() {
        this.names = SimplyNicks.getDatabase().getAllNames();
    }
    
    public void addNewName(final String uuid, final String name) {
        this.names.put(uuid, name);
    }
    
    public void removeName(final String uuid) {
        this.names.remove(uuid);
    }
    
    public String getName(final String playerUUID) {
        return this.names.get(playerUUID);
    }
    
    public boolean isNameAvailable(String name) {
        name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
        for (final Map.Entry<String, String> entry : this.names.entrySet()) {
            if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String)entry.getValue())).equals(name)) {
                return false;
            }
        }
        for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isNameAvailable(final String UUID, String name) {
        name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
        for (final Map.Entry<String, String> entry : this.names.entrySet()) {
            if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String)entry.getValue())).equals(name) && !entry.getKey().equals(UUID)) {
                return false;
            }
        }
        for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(name) && !onlinePlayer.getUniqueId().toString().equals(UUID)) {
                return false;
            }
        }
        return true;
    }
    
    public Set<String> getNames() {
        return this.names.keySet();
    }
    
    public String getUUIDByName(final String name) {
        for (final Map.Entry<String, String> entry : this.names.entrySet()) {
            if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String)entry.getValue())).equals(name)) {
                return entry.getKey();
            }
        }
        throw new NullPointerException("There is no UUID about " + name);
    }
}
