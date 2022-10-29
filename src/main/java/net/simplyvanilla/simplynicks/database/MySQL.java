package net.simplyvanilla.simplynicks.database;

import net.simplyvanilla.simplynicks.SimplyNicks;

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
                            `id` BINARY(16) NOT NULL,
                            `nick` varchar(256) NOT NULL,
                            `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                             UNIQUE KEY `nick` (`nick`)
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
        String query = String.format("SELECT BIN_TO_UUID(`id`) `id`, `nick` FROM `%s`", this.tableName);

        try {
            ResultSet rs = this.statement.executeQuery(query);

            while (rs.next()) {
                nicks.put(rs.getString("id"), rs.getString("nick"));
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getAllNicks...");
            ex.printStackTrace();
        }

        return nicks;
    }

    public String getPlayerNickData(UUID uuid) {
        String playerSearchQuery = String.format("SELECT `nick` FROM `%s` WHERE `id` = UUID_TO_BIN(?)", this.tableName);

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

    public boolean updatePlayerNickData(UUID uuid, String nick) {
        if (getPlayerNickData(uuid) == null) {
            String playerNickDataQuery = String.format("INSERT INTO `%s` (`id`, `nick`) VALUES (UUID_TO_BIN(?), ?)", this.tableName);

            try {
                PreparedStatement playerNickDataQueryPS = this.connection.prepareStatement(playerNickDataQuery);
                playerNickDataQueryPS.setString(1, uuid.toString());
                playerNickDataQueryPS.setString(2, nick);
                playerNickDataQueryPS.executeUpdate();
            } catch (Exception ex) {
                this.plugin.getLogger().log(Level.SEVERE, "Unable to insertPlayerNickData...");
                ex.printStackTrace();
                return false;
            }

        } else {
            String playerNickDataQuery = String.format("UPDATE `%s` SET `nick` = ? WHERE `id` = UUID_TO_BIN(?)", this.tableName);

            try {
                PreparedStatement playerNickDataQueryPS = this.connection.prepareStatement(playerNickDataQuery);
                playerNickDataQueryPS.setString(1, nick);
                playerNickDataQueryPS.setString(2, uuid.toString());
                playerNickDataQueryPS.executeUpdate();
            } catch (Exception ex) {
                this.plugin.getLogger().log(Level.SEVERE, "Unable to updatePlayerNickData...");
                ex.printStackTrace();
                return false;
            }
        }

        SimplyNicks.getCache().removeNick(uuid.toString());
        SimplyNicks.getCache().addNick(uuid.toString(), nick);
        return true;
    }

    public void removePlayerNickData(UUID uuid) {
        String playerListUpdateQuery = String.format("DELETE FROM `%s` WHERE `id` = UUID_TO_BIN(?)", this.tableName);

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
