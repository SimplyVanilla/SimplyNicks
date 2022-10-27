// 
// Decompiled by Procyon v0.5.36
// 

package me.orbitium.database;

import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import java.sql.PreparedStatement;
import org.bukkit.Bukkit;
import java.util.UUID;
import java.sql.ResultSet;
import org.bukkit.ChatColor;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.Plugin;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.sql.Statement;
import java.sql.Connection;
import me.orbitium.SimplyNicks;

public class MYSQL
{
    SimplyNicks plugin;
    String tableName;
    Connection connection;
    Statement statement;
    
    public MYSQL() {
        this.plugin = SimplyNicks.getInstance();
        this.tableName = this.plugin.getConfig().getString("database.playerNameStorageTableName");
    }
    
    public synchronized void connect() {
        try {
            this.plugin.getLogger().log(Level.INFO, "Connecting to MYSQL server, please wait...");
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection(this.plugin.getConfig().getString("database.url"), this.plugin.getConfig().getString("database.username"), this.plugin.getConfig().getString("database.password"));
            this.statement = this.connection.createStatement();
            final String tableCheckQuery = String.format("    CREATE TABLE IF NOT EXISTS `%s` (\n        `id` int unsigned NOT NULL AUTO_INCREMENT,\n        `uuid` char(36) NOT NULL,\n        `name` text NOT NULL,\n        `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,\n        `updated_at` timestamp ,\n        PRIMARY KEY (`id`),\n        UNIQUE KEY `uuid` (`uuid`)\n    )\n", this.tableName);
            this.statement.executeUpdate(tableCheckQuery);
            this.plugin.getLogger().log(Level.INFO, "Connected to the MYSQL server!");
        }
        catch (Exception ignored) {
            ignored.printStackTrace();
        }
        if (this.connection == null || this.statement == null) {
            this.plugin.getLogger().log(Level.SEVERE, "Database connection is not stable. Plugin disabling...");
            this.plugin.getLogger().log(Level.SEVERE, "Please check your database. And config file.");
            this.plugin.getPluginLoader().disablePlugin((Plugin)this.plugin);
        }
    }
    
    public Map<String, String> getAllNames() {
        final Map<String, String> names = new HashMap<String, String>();
        final String query = String.format("Select * from %s", this.tableName);
        try {
            final ResultSet rs = this.statement.executeQuery(query);
            while (rs.next()) {
                names.put(rs.getString("uuid"), ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', rs.getString("name"))));
            }
        }
        catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getPlayerNameData...");
            ex.printStackTrace();
        }
        return names;
    }
    
    public String getPlayerNameData(final String playerUUID) {
        final String playerSearchQuery = String.format("SELECT * FROM `%s` WHERE `uuid` =?", this.tableName);
        try {
            final PreparedStatement playerSearchQueryPS = this.connection.prepareStatement(playerSearchQuery);
            try {
                playerSearchQueryPS.setString(1, playerUUID);
                final ResultSet rs = playerSearchQueryPS.executeQuery();
                try {
                    if (rs.next()) {
                        final String string = rs.getString("name");
                        if (rs != null) {
                            rs.close();
                        }
                        if (playerSearchQueryPS != null) {
                            playerSearchQueryPS.close();
                        }
                        return string;
                    }
                    if (rs != null) {
                        rs.close();
                    }
                }
                catch (Throwable t) {
                    if (rs != null) {
                        try {
                            rs.close();
                        }
                        catch (Throwable exception) {
                            t.addSuppressed(exception);
                        }
                    }
                    throw t;
                }
                if (playerSearchQueryPS != null) {
                    playerSearchQueryPS.close();
                }
            }
            catch (Throwable t2) {
                if (playerSearchQueryPS != null) {
                    try {
                        playerSearchQueryPS.close();
                    }
                    catch (Throwable exception2) {
                        t2.addSuppressed(exception2);
                    }
                }
                throw t2;
            }
        }
        catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getPlayerNameData...");
            ex.printStackTrace();
        }
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
        if (offlinePlayer.getName() == null) {
            throw new NullPointerException();
        }
        this.updatePlayerNameData(playerUUID, offlinePlayer.getName());
        return offlinePlayer.getName();
    }
    
    public void updatePlayerNameData(final Player player, final String newName) {
        final String playerListUpdateQuery = String.format("    INSERT INTO `%s` (`uuid`, `name`) VALUES (?, ?)\n    ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `updated_at` = CURRENT_TIMESTAMP\n", this.tableName);
        try {
            final PreparedStatement playerListUpdateQueryPS = this.connection.prepareStatement(playerListUpdateQuery);
            try {
                playerListUpdateQueryPS.setString(1, player.getUniqueId().toString());
                playerListUpdateQueryPS.setString(2, newName);
                playerListUpdateQueryPS.executeUpdate();
                if (playerListUpdateQueryPS != null) {
                    playerListUpdateQueryPS.close();
                }
            }
            catch (Throwable t) {
                if (playerListUpdateQueryPS != null) {
                    try {
                        playerListUpdateQueryPS.close();
                    }
                    catch (Throwable exception) {
                        t.addSuppressed(exception);
                    }
                }
                throw t;
            }
        }
        catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to updatePlayerNameData...");
            ex.printStackTrace();
        }
        SimplyNicks.getCache().removeName(player.getUniqueId().toString());
        SimplyNicks.getCache().addNewName(player.getUniqueId().toString(), newName);
    }
    
    public void updatePlayerNameData(final String playerUUID, final String newName) {
        final String playerListUpdateQuery = String.format("    INSERT INTO `%s` (`uuid`, `name`) VALUES (?, ?)\n    ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `updated_at` = CURRENT_TIMESTAMP\n", this.tableName);
        try {
            final PreparedStatement playerListUpdateQueryPS = this.connection.prepareStatement(playerListUpdateQuery);
            try {
                playerListUpdateQueryPS.setString(1, playerUUID);
                playerListUpdateQueryPS.setString(2, newName);
                playerListUpdateQueryPS.executeUpdate();
                if (playerListUpdateQueryPS != null) {
                    playerListUpdateQueryPS.close();
                }
            }
            catch (Throwable t) {
                if (playerListUpdateQueryPS != null) {
                    try {
                        playerListUpdateQueryPS.close();
                    }
                    catch (Throwable exception) {
                        t.addSuppressed(exception);
                    }
                }
                throw t;
            }
        }
        catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to updatePlayerNameData...");
            ex.printStackTrace();
        }
        SimplyNicks.getCache().removeName(playerUUID);
        SimplyNicks.getCache().addNewName(playerUUID, newName);
    }
    
    public void close() {
        try {
            this.connection.close();
        }
        catch (Exception ex) {}
    }
}
