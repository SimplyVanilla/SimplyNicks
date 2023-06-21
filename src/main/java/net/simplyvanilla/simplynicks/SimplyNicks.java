package net.simplyvanilla.simplynicks;

import java.util.List;
import net.simplyvanilla.simplynicks.commands.NickCommandExecutor;
import net.simplyvanilla.simplynicks.commands.RealnameCommandExecutor;
import net.simplyvanilla.simplynicks.database.Cache;
import net.simplyvanilla.simplynicks.database.MySQL;
import net.simplyvanilla.simplynicks.event.PlayerEvents;
import org.bukkit.plugin.java.JavaPlugin;

public class SimplyNicks extends JavaPlugin {
    private static SimplyNicks instance;
    private MySQL database;
    private Cache cache;
    private List<String> colors;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        instance = this;
        this.database = new MySQL();
        this.cache = new Cache();

        try {
            this.database.connect();
        } catch (Exception e) {
            getLogger().warning(
                "Could not connect to database! Please check your config.yml and try again.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            this.cache.initCache();
        } catch (Exception e) {
            getLogger().warning(
                "Could not load cache! Please check your config.yml and try again.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        this.getCommand("nick").setExecutor(new NickCommandExecutor());
        this.getCommand("realname").setExecutor(new RealnameCommandExecutor());
        colors = this.getConfig().getStringList("colors");
    }

    @Override
    public void onDisable() {
        database.close();
    }

    public static SimplyNicks getInstance() {
        return instance;
    }

    public MySQL getDatabase() {
        return this.database;
    }

    public Cache getCache() {
        return this.cache;
    }

    public List<String> getColors() {
        return colors;
    }
}
