package net.simplyvanilla.simplynicks.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import net.simplyvanilla.simplynicks.SimplyNicks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class MYSQL {
    SimplyNicks plugin = SimplyNicks.getInstance();
    String tableName;
    Connection connection;
    Statement statement;

    public MYSQL() {
        this.tableName = this.plugin.getConfig().getString("database.player_nick");
    }

    public synchronized void connect() {
        try {
            this.plugin.getLogger().log(Level.INFO, "Connecting to MYSQL server, please wait...");
            this.connection = DriverManager.getConnection(
                Objects.requireNonNull(this.plugin.getConfig().getString("database.url")),
                this.plugin.getConfig().getString("database.username"),
                this.plugin.getConfig().getString("database.password"));
            this.statement = this.connection.createStatement();

            String tableCheckQuery = String.format(
                """
                    CREATE TABLE IF NOT EXISTS `%s` (
                        `id` int unsigned NOT NULL AUTO_INCREMENT,
                        `uuid` char(36) NOT NULL,
                        `nick` varchar(256) NOT NULL,
                        `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                         UNIQUE KEY `uuid` (`uuid`)
                    )
                """, this.tableName);

            this.statement.executeUpdate(tableCheckQuery);
            this.plugin.getLogger().log(Level.INFO, "Connected to the MYSQL server!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (this.connection == null || this.statement == null) {
            this.plugin.getLogger().log(Level.SEVERE, "Database connection is not stable. Plugin disabling...");
            this.plugin.getLogger().log(Level.SEVERE, "Please check your database. And config file.");
            this.plugin.getPluginLoader().disablePlugin(this.plugin);
        }

    }

    public Map<String, String> getAllNames() {
        Map<String, String> names = new HashMap<>();
        String query = String.format("Select * from %s", this.tableName);

        try {
            ResultSet rs = this.statement.executeQuery(query);

            while(rs.next()) {
                names.put(
                    rs.getString("uuid"),
                    ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', rs.getString("name"))));
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getPlayerNameData...");
            ex.printStackTrace();
        }

        return names;
    }

    public String getPlayerNameData(String playerUUID) {
        String playerSearchQuery = String.format("SELECT * FROM `%s` WHERE `uuid` = ?", this.tableName);

        try {
            PreparedStatement playerSearchQueryPS = this.connection.prepareStatement(playerSearchQuery);
            playerSearchQueryPS.setString(1, playerUUID);
            ResultSet rs = playerSearchQueryPS.executeQuery();
            if (rs.next()) {
                return rs.getString("nick");
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getPlayerNameData...");
            ex.printStackTrace();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
        if (offlinePlayer.getName() == null) {
            throw new NullPointerException();
        } else {
            this.updatePlayerNameData(playerUUID, offlinePlayer.getName());
            return offlinePlayer.getName();
        }
    }

    public void updatePlayerNameData(Player player, String newName) {
        this.updatePlayerNameData(player.getUniqueId().toString(), newName);
    }

    public void updatePlayerNameData(String playerUUID, String newName) {
        String playerListUpdateQuery = String.format(
            """
                INSERT INTO `%s` (`uuid`, `name`) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `updated_at` = CURRENT_TIMESTAMP
            """, this.tableName);

        try {
            PreparedStatement playerListUpdateQueryPS = this.connection.prepareStatement(playerListUpdateQuery);
            playerListUpdateQueryPS.setString(1, playerUUID);
            playerListUpdateQueryPS.setString(2, newName);
            playerListUpdateQueryPS.executeUpdate();
        } catch (Exception var5) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to updatePlayerNameData...");
            var5.printStackTrace();
        }

        SimplyNicks.getCache().removeName(playerUUID);
        SimplyNicks.getCache().addNewName(playerUUID, newName);
    }

    public void close() {
        try {
            this.connection.close();
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.INFO, "MYSQL database is closing...");
            ex.printStackTrace();
        }

    }
}
