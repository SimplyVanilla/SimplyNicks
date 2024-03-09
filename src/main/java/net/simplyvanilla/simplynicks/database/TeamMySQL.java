package net.simplyvanilla.simplynicks.database;

import net.simplyvanilla.simplynicks.SimplyNicks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;

public class TeamMySQL {
    SimplyNicks plugin;
    String teamTableName;
    String playerTeamTableName;
    Connection connection;
    Statement statement;

    public TeamMySQL(SimplyNicks plugin) {
        this.plugin = plugin;
        this.teamTableName = this.plugin.getConfig().getString("database.teamTableName");
        this.playerTeamTableName = this.plugin.getConfig().getString("database.playerTeamTableName");
    }

    public synchronized void connect() {
        try {
            this.plugin.getLogger().log(Level.INFO, "Connecting to MySQL server, please wait...");
            this.connection = DriverManager.getConnection(
                Objects.requireNonNull(this.plugin.getConfig().getString("database.url")),
                this.plugin.getConfig().getString("database.username"),
                this.plugin.getConfig().getString("database.password"));

            PreparedStatement teamTableCheckQuery = this.connection.prepareStatement(
                """
                        CREATE TABLE IF NOT EXISTS ? (
                            `id` BINARY(16) NOT NULL,
                            `name` varchar(256) NOT NULL,
                            `color` varchar(256) NOT NULL,
                            PRIMARY KEY (`id`)
                        );
                    """
            );
            teamTableCheckQuery.setString(1, this.teamTableName);
            teamTableCheckQuery.executeUpdate();

            PreparedStatement playerTeamTableCheckQuery = this.connection.prepareStatement(
                """
                        CREATE TABLE IF NOT EXISTS ? (
                            `id` BINARY(16) NOT NULL,
                            `team_id` BINARY(16) NOT NULL,
                            PRIMARY KEY (`id`),
                            FOREIGN KEY (`team_id`) REFERENCES ?(`id`) ON DELETE CASCADE
                        );
                    """
            );
            playerTeamTableCheckQuery.setString(1, this.playerTeamTableName);
            playerTeamTableCheckQuery.setString(2, this.teamTableName);
            playerTeamTableCheckQuery.executeUpdate();

            this.plugin.getLogger().log(Level.INFO, "Connected to the MySQL server!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (this.connection == null || this.statement == null) {
            this.plugin.getLogger()
                .log(Level.SEVERE, "Database connection is not stable. Plugin disabling...");
            this.plugin.getLogger()
                .log(Level.SEVERE, "Please check your database. And config file.");
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
        }
    }

    public synchronized void close() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean createTeam(UUID uuid, String name) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            """
                    INSERT INTO ? (id, name, color) VALUES (UUID_TO_BIN(?), ?, ?);
                """
        )) {
            preparedStatement.setString(1, this.teamTableName);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.setString(3, name);
            preparedStatement.setString(4, "");
            preparedStatement.executeUpdate();
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to createTeam...", ex);
            return false;
        }
    }

    public boolean modifyTeam(UUID uuid, String modifyType, String value) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            """
                    UPDATE ? SET ? = ? WHERE id = UUID_TO_BIN(?);
                """
        )) {
            preparedStatement.setString(1, this.teamTableName);
            Runnable afterRun;
            if (modifyType.equalsIgnoreCase("name")) {
                preparedStatement.setString(2, "name");
                afterRun = () -> this.plugin.getTeamCache().updateTeamByOwner(uuid, playerTeam -> playerTeam.setName(value));
            } else if (modifyType.equalsIgnoreCase("color")) {
                preparedStatement.setString(2, "color");
                afterRun = () -> this.plugin.getTeamCache().updateTeamByOwner(uuid, playerTeam -> playerTeam.setColor(value));
            } else {
                return false;
            }
            preparedStatement.setString(3, value);
            preparedStatement.setString(4, uuid.toString());
            preparedStatement.executeUpdate();
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to modifyTeam...", ex);
            return false;
        }
    }

    public boolean joinTeam(UUID uuid, UUID teamId) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            """
                    INSERT INTO ? (id, team_id) VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?);
                """
        )) {
            preparedStatement.setString(1, this.playerTeamTableName);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.setString(3, teamId.toString());
            preparedStatement.executeUpdate();
            this.plugin.getTeamCache().addTeam(uuid, getTeam(teamId));
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to joinTeam...", ex);
            return false;
        }
    }

    public boolean leaveTeam(UUID uuid, UUID teamId) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            """
                    DELETE FROM ? WHERE id = UUID_TO_BIN(?) AND team_id = UUID_TO_BIN(?);
                """
        )) {
            preparedStatement.setString(1, this.playerTeamTableName);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.setString(3, teamId.toString());
            preparedStatement.executeUpdate();
            this.plugin.getTeamCache().removeTeam(uuid);
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to leaveTeam...", ex);
            return false;
        }
    }

    public boolean deleteTeam(UUID uuid) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            """
                    DELETE FROM ? WHERE id = UUID_TO_BIN(?);
                """
        )) {
            preparedStatement.setString(1, this.teamTableName);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
            this.plugin.getTeamCache().removeTeamByOwner(uuid);
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to deleteTeam...", ex);
            return false;
        }
    }

    public Map<UUID, PlayerTeam> getAllTeams() {
        Map<UUID, PlayerTeam> teams = new HashMap<>();

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            """
                SELECT BIN_TO_UUID(pt.id) AS player_id, BIN_TO_UUID(t.id) AS owner_id, t.name AS team_name, t.color AS team_color
                FROM `?` AS pt
                JOIN `?` AS t ON pt.team_id = t.id;
                """
        )) {
            preparedStatement.setString(1, this.playerTeamTableName);
            preparedStatement.setString(2, this.teamTableName);
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                UUID playerId = UUID.fromString(resultSet.getString("player_id"));
                UUID ownerId = UUID.fromString(resultSet.getString("owner_id"));
                String teamName = resultSet.getString("team_name");
                String teamColor = resultSet.getString("team_color");
                teams.put(playerId, new PlayerTeam(playerId, teamName, teamColor, ownerId));
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getAllTeams...", ex);
        }

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            """
                SELECT BIN_TO_UUID(id) AS id, name, color
                FROM ?;
                """
        )) {
            preparedStatement.setString(1, this.teamTableName);
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                UUID id = UUID.fromString(resultSet.getString("id"));
                String name = resultSet.getString("name");
                String color = resultSet.getString("color");
                teams.put(id, new PlayerTeam(id, name, color, id));
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getAllTeams...", ex);
        }

        return teams;
    }

    public PlayerTeam getTeam(UUID uuid) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            """
                SELECT BIN_TO_UUID(id) AS id, name, color
                FROM ? WHERE id = UUID_TO_BIN(?);
                """
        )) {
            preparedStatement.setString(1, this.teamTableName);
            preparedStatement.setString(2, uuid.toString());
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                UUID id = UUID.fromString(resultSet.getString("id"));
                String name = resultSet.getString("name");
                String color = resultSet.getString("color");
                return new PlayerTeam(id, name, color, id);
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getTeam...", ex);
        }

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            """
                SELECT BIN_TO_UUID(pt.id) AS player_id, BIN_TO_UUID(t.id) AS owner_id, t.name AS team_name, t.color AS team_color
                FROM `?` AS pt
                JOIN `?` AS t ON pt.team_id = t.id
                WHERE pt.id = UUID_TO_BIN(?);
                """
        )) {
            preparedStatement.setString(1, this.playerTeamTableName);
            preparedStatement.setString(2, this.teamTableName);
            preparedStatement.setString(3, uuid.toString());
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                UUID playerId = UUID.fromString(resultSet.getString("player_id"));
                UUID ownerId = UUID.fromString(resultSet.getString("owner_id"));
                String teamName = resultSet.getString("team_name");
                String teamColor = resultSet.getString("team_color");
                return new PlayerTeam(playerId, teamName, teamColor, ownerId);
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getTeam...", ex);
        }

        return null;
    }

    public static final class PlayerTeam {
        private UUID uuid;
        private String name;
        private String color;
        private UUID owner;

        public PlayerTeam(UUID uuid, String name, String color, UUID owner) {
            this.uuid = uuid;
            this.name = name;
            this.color = color;
            this.owner = owner;
        }

        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public UUID getOwner() {
            return owner;
        }

        public void setOwner(UUID owner) {
            this.owner = owner;
        }

        public boolean isOwner() {
            return this.uuid.equals(this.owner);
        }
    }
}
