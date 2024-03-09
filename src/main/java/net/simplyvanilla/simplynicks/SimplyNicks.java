package net.simplyvanilla.simplynicks;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.simplyvanilla.simplynicks.commands.NickCommandExecutor;
import net.simplyvanilla.simplynicks.commands.RealnameCommandExecutor;
import net.simplyvanilla.simplynicks.commands.TeamCommandExecutor;
import net.simplyvanilla.simplynicks.database.Cache;
import net.simplyvanilla.simplynicks.database.MySQL;
import net.simplyvanilla.simplynicks.database.TeamCache;
import net.simplyvanilla.simplynicks.database.TeamMySQL;
import net.simplyvanilla.simplynicks.event.PlayerEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimplyNicks extends JavaPlugin {
    private MySQL database;
    private Cache cache;
    private List<String> colors;
    private TeamMySQL teamDatabase;
    private TeamCache teamCache;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.database = new MySQL(this);
        this.cache = new Cache(this);

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

        this.teamDatabase = new TeamMySQL(this);
        this.teamCache = new TeamCache(this);

        try {
            this.teamDatabase.connect();
        } catch (Exception e) {
            getLogger().warning("Could not connect to database! Please check your config.yml and try again.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            this.teamCache.initCache();
        } catch (Exception e) {
            getLogger().warning("Could not load cache! Please check your config.yml and try again.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getServer().getPluginManager().registerEvents(new PlayerEvents(this), this);
        this.getCommand("nick").setExecutor(new NickCommandExecutor(this));
        this.getCommand("realname").setExecutor(new RealnameCommandExecutor(this));
        this.getCommand("team").setExecutor(new TeamCommandExecutor(this));
        colors = this.getConfig().getStringList("colors");

        if (Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders")) {
            Expansion.builder("simplynicks")
                .filter(Player.class)
                .audiencePlaceholder("team_name", (audience, ctx, queue) -> {
                    Player player = (Player) audience;
                    TeamMySQL.PlayerTeam playerTeam = teamCache.getTeam(player.getUniqueId());
                    Component teamName = playerTeam == null ? Component.text("") : Component.text(playerTeam.getName());
                    return Tag.selfClosingInserting(teamName);
                })
                .audiencePlaceholder("team_color", (audience, ctx, queue) -> {
                    Player player = (Player) audience;
                    TeamMySQL.PlayerTeam playerTeam = teamCache.getTeam(player.getUniqueId());
                    Component teamColor = playerTeam == null ? Component.text("") : Component.text(playerTeam.getColor());
                    return Tag.selfClosingInserting(teamColor);
                })
                .audiencePlaceholder("team_prefix", (audience, ctx, queue) -> {
                    Player player = (Player) audience;
                    TeamMySQL.PlayerTeam playerTeam = teamCache.getTeam(player.getUniqueId());
                    Component teamPrefix = playerTeam == null
                        ? Component.text("")
                        : Component.text(playerTeam.getName()).color(NamedTextColor.NAMES.valueOr(playerTeam.getName(), NamedTextColor.WHITE));
                    return Tag.selfClosingInserting(teamPrefix);
                })
                .build()
                .register();
        }
    }

    @Override
    public void onDisable() {
        database.close();
        teamDatabase.close();
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

    public TeamMySQL getTeamDatabase() {
        return teamDatabase;
    }

    public TeamCache getTeamCache() {
        return teamCache;
    }

    public String getMessage(String path) {
        return Optional.ofNullable(this.getConfig().getString(path)).orElse(path);
    }

    public void sendConfigMessage(CommandSender commandSender, String message) {
        sendConfigMessage(commandSender, message, new HashMap<>());
    }

    public void sendConfigMessage(CommandSender commandSender, String message,
                                  Map<String, String> replacements) {
        message = getMessage(message);
        commandSender.sendMessage(MiniMessage.miniMessage()
            .deserialize(message, replacements.entrySet().stream()
                .map(entry -> {
                    if (entry.getValue().contains("&")) {
                        return Placeholder.component(entry.getKey(),
                            LegacyComponentSerializer.legacyAmpersand()
                                .deserialize(entry.getValue()));
                    }

                    return Placeholder.unparsed(entry.getKey(),
                        entry.getValue());
                }).toList().toArray(TagResolver[]::new)));
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

