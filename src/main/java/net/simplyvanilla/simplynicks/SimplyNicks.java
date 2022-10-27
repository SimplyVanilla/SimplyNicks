package net.simplyvanilla.simplynicks;

import java.util.List;
import java.util.logging.Level;
import net.simplyvanilla.simplynicks.command.NickCommandExecutor;
import net.simplyvanilla.simplynicks.database.Cache;
import net.simplyvanilla.simplynicks.database.MYSQL;
import net.simplyvanilla.simplynicks.event.PlayerEvents;
import net.simplyvanilla.simplynicks.hook.PlaceholderAPIHook;
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
        this.getCommand("nick").setExecutor(new NickCommandExecutor());
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