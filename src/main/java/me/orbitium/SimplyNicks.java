/* Decompiler 3ms, total 654ms, lines 50 */
package me.orbitium;

import java.util.List;
import java.util.logging.Level;
import me.orbitium.command.NameCommandExecutor;
import me.orbitium.database.Cache;
import me.orbitium.database.MYSQL;
import me.orbitium.event.PlayerEvents;
import me.orbitium.hook.PlaceholderAPIHook;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimplyNicks extends JavaPlugin {
    private static SimplyNicks instance;
    private static MYSQL database;
    private static Cache cache;
    public static List<String> colors;

    public void onEnable() {
        this.saveDefaultConfig();
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            this.getLogger().log(Level.SEVERE, "Could not find PlaceholderAPI! This plugin is required.");
            this.getServer().getPluginManager().disablePlugin(this);
        }

        instance = this;
        (database = new MYSQL()).connect();
        (cache = new Cache()).initCache();
        (new PlaceholderAPIHook()).register();
        this.getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        this.getCommand("name").setExecutor(new NameCommandExecutor());
        colors = this.getConfig().getStringList("colors");
    }

    public void onDisable() {
        database.close();
    }

    public static SimplyNicks getInstance() {
        return instance;
    }

    public static MYSQL getDatabase() {
        return database;
    }

    public static Cache getCache() {
        return cache;
    }
}