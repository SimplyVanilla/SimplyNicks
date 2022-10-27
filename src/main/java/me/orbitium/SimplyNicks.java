// 
// Decompiled by Procyon v0.5.36
// 

package me.orbitium;

import org.bukkit.command.CommandExecutor;
import me.orbitium.command.NameCommandExecutor;
import org.bukkit.event.Listener;
import me.orbitium.event.PlayerEvents;
import me.orbitium.hook.PlaceholderAPIHook;
import org.bukkit.plugin.Plugin;
import java.util.logging.Level;
import java.util.List;
import me.orbitium.database.Cache;
import me.orbitium.database.MYSQL;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimplyNicks extends JavaPlugin
{
    private static SimplyNicks instance;
    private static MYSQL database;
    private static Cache cache;
    public static List<String> colors;
    
    public void onEnable() {
        this.saveDefaultConfig();
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            this.getLogger().log(Level.SEVERE, "Could not find PlaceholderAPI! This plugin is required.");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
        }
        SimplyNicks.instance = this;
        (SimplyNicks.database = new MYSQL()).connect();
        (SimplyNicks.cache = new Cache()).initCache();
        new PlaceholderAPIHook().register();
        this.getServer().getPluginManager().registerEvents((Listener)new PlayerEvents(), (Plugin)this);
        this.getCommand("name").setExecutor((CommandExecutor)new NameCommandExecutor());
        SimplyNicks.colors = (List<String>)this.getConfig().getStringList("colors").stream().toList();
    }
    
    public void onDisable() {
        SimplyNicks.database.close();
    }
    
    public static SimplyNicks getInstance() {
        return SimplyNicks.instance;
    }
    
    public static MYSQL getDatabase() {
        return SimplyNicks.database;
    }
    
    public static Cache getCache() {
        return SimplyNicks.cache;
    }
}
