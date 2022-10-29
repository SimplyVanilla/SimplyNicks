package net.simplyvanilla.simplynicks;

import net.simplyvanilla.simplynicks.command.NickCommandExecutor;
import net.simplyvanilla.simplynicks.database.Cache;
import net.simplyvanilla.simplynicks.database.MySQL;
import net.simplyvanilla.simplynicks.event.PlayerEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class SimplyNicks extends JavaPlugin {
    private static SimplyNicks instance;
    private static MySQL database;
    private static Cache cache;
    public static List<String> colors;

    public void onEnable() {
        this.saveDefaultConfig();

        instance = this;
        (database = new MySQL()).connect();
        // @todo disable plugin if unable to init cache
        (cache = new Cache()).initCache();
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

    public static MySQL getDatabase() {
        return database;
    }

    public static Cache getCache() {
        return cache;
    }
}
