package net.simplyvanilla.simplynicks.database;

import net.simplyvanilla.simplynicks.SimplyNicks;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class MySQL {
    SimplyNicks plugin = SimplyNicks.getInstance();
    String tableName;
    Connection connection;
    Statement statement;

    public MySQL() {
        this.tableName = this.plugin.getConfig().getString("database.nickTableName");
    }

    public synchronized void connect() {
        try {
            this.plugin.getLogger().log(Level.INFO, "Connecting to MySQL server, please wait...");
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
            this.plugin.getLogger().log(Level.INFO, "Connected to the MySQL server!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (this.connection == null || this.statement == null) {
            this.plugin.getLogger().log(Level.SEVERE, "Database connection is not stable. Plugin disabling...");
            this.plugin.getLogger().log(Level.SEVERE, "Please check your database. And config file.");
            this.plugin.getPluginLoader().disablePlugin(this.plugin);
        }

    }

    public Map<String, String> getAllNicks() {
        Map<String, String> nicks = new HashMap<>();
        String query = String.format("SELECT * FROM `%s`", this.tableName);

        try {
            ResultSet rs = this.statement.executeQuery(query);

            while (rs.next()) {
                nicks.put(rs.getString("uuid"), rs.getString("nick"));
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getAllNicks...");
            ex.printStackTrace();
        }

        return nicks;
    }

    public String getPlayerNickData(UUID uuid) {
        String playerSearchQuery = String.format("SELECT * FROM `%s` WHERE `uuid` = ?", this.tableName);

        try {
            PreparedStatement playerSearchQueryPS = this.connection.prepareStatement(playerSearchQuery);
            playerSearchQueryPS.setString(1, uuid.toString());
            ResultSet rs = playerSearchQueryPS.executeQuery();
            if (rs.next()) {
                return rs.getString("nick");
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getPlayerNickData...");
            ex.printStackTrace();
        }

        return null;
    }

    public void updatePlayerNickData(UUID uuid, String nick) {
        String playerListUpdateQuery = String.format(
            """
                    INSERT INTO `%s` (`uuid`, `nick`) VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE `nick` = VALUES(`nick`), `updated_at` = CURRENT_TIMESTAMP
                """, this.tableName);

        try {
            PreparedStatement playerListUpdateQueryPS = this.connection.prepareStatement(playerListUpdateQuery);
            playerListUpdateQueryPS.setString(1, uuid.toString());
            playerListUpdateQueryPS.setString(2, nick);
            playerListUpdateQueryPS.executeUpdate();
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to updatePlayerNickData...");
            ex.printStackTrace();
        }

        SimplyNicks.getCache().removeNick(uuid.toString());
        SimplyNicks.getCache().addNick(uuid.toString(), nick);
    }

    public void removePlayerNickData(UUID uuid) {
        String playerListUpdateQuery = String.format("DELETE FROM `%s` WHERE `uuid` = ?", this.tableName);

        try {
            PreparedStatement playerListUpdateQueryPS = this.connection.prepareStatement(playerListUpdateQuery);
            playerListUpdateQueryPS.setString(1, uuid.toString());
            playerListUpdateQueryPS.executeUpdate();
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to removePlayerNickData...");
            ex.printStackTrace();
        }

        SimplyNicks.getCache().removeNick(uuid.toString());
    }

    public void close() {
        try {
            this.connection.close();
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.INFO, "MySQL database is closing...");
            ex.printStackTrace();
        }

    }
}
