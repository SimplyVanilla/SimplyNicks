package net.simplyvanilla.simplynicks.database;

import net.simplyvanilla.simplynicks.SimplyNicks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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

            this.statement = this.connection.createStatement();

            String teamTableCheckQuery = String.format(
                """
                        CREATE TABLE IF NOT EXISTS `%s` (
                            `id` INT NOT NULL AUTO_INCREMENT,
                            `name` varchar(256) NOT NULL,
                            `color` varchar(256) NOT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE INDEX `name` (`name`)
                        );
                    """, this.teamTableName
            );
            this.statement.executeUpdate(teamTableCheckQuery);

            String playerTeamTableCheckQuery = String.format(
                """
                        CREATE TABLE IF NOT EXISTS `%s` (
                            `id` BINARY(16) NOT NULL,
                            `team_id` INT NOT NULL,
                            PRIMARY KEY (`id`),
                            FOREIGN KEY (`team_id`) REFERENCES `%s`(`id`) ON DELETE CASCADE
                        );
                    """, this.playerTeamTableName, this.teamTableName
            );
            this.statement.executeUpdate(playerTeamTableCheckQuery);

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

    public int getTeamId(String name) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            String.format("SELECT `id` FROM `%s` WHERE `name` = ?", this.teamTableName)
        )) {
            preparedStatement.setString(1, name);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getTeamId...", ex);
        }
        return -1;
    }

    public boolean createTeam(String name) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            String.format("INSERT INTO `%s` (`name`, `color`) VALUES (?, ?)", this.teamTableName)
        )) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, "");
            preparedStatement.executeUpdate();
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to createTeam...", ex);
            return false;
        }
    }

    public boolean deleteTeam(int teamId) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            String.format("DELETE FROM `%s` WHERE id = ?", this.teamTableName)
        )) {
            preparedStatement.setInt(1, teamId);
            preparedStatement.executeUpdate();
            this.plugin.getTeamCache().removeTeamByTeamId(teamId);
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to deleteTeam...", ex);
            return false;
        }
    }

    public boolean modifyTeam(int teamId, String modifyType, String value) {
        if (!modifyType.equalsIgnoreCase("name") &&
            !modifyType.equalsIgnoreCase("color")) {
            return false;
        }

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            String.format("UPDATE `%s` SET `%s` = ? WHERE `id` = ?", this.teamTableName, modifyType.toLowerCase())
        )) {
            Runnable afterRun;
            if (modifyType.equalsIgnoreCase("name")) {
                afterRun = () -> this.plugin.getTeamCache().updateTeam(teamId, team -> team.setName(value));
            } else if (modifyType.equalsIgnoreCase("color")) {
                afterRun = () -> this.plugin.getTeamCache().updateTeam(teamId, team -> team.setColor(value));
            } else {
                return false;
            }
            preparedStatement.setString(1, value);
            preparedStatement.setInt(2, teamId);
            preparedStatement.executeUpdate();
            afterRun.run();
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to modifyTeam...", ex);
            return false;
        }
    }

    public boolean joinTeam(UUID uuid, int teamId) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            String.format("INSERT INTO `%s` (`id`, `team_id`) VALUES (UUID_TO_BIN(?), ?)", this.playerTeamTableName)
        )) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setInt(2, teamId);
            preparedStatement.executeUpdate();
            this.plugin.getTeamCache().addTeam(uuid, getTeam(uuid));
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to joinTeam...", ex);
            return false;
        }
    }

    public boolean leaveTeam(UUID uuid, int teamId) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            String.format("DELETE FROM `%s` WHERE `id` = UUID_TO_BIN(?) AND `team_id` = ?", this.playerTeamTableName)
        )) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setInt(2, teamId);
            preparedStatement.executeUpdate();
            this.plugin.getTeamCache().removeTeam(uuid);
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to leaveTeam...", ex);
            return false;
        }
    }

    public Map<UUID, PlayerTeam> getAllTeams() {
        Map<UUID, PlayerTeam> teams = new HashMap<>();

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            String.format(
                """
                        SELECT BIN_TO_UUID(pt.id) AS player_id, t.id AS team_id, t.name AS team_name, t.color AS team_color
                        FROM `%s` AS pt
                        JOIN `%s` AS t ON pt.team_id = t.id;
                    """, this.playerTeamTableName, this.teamTableName)
        )) {
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                UUID playerId = UUID.fromString(resultSet.getString("player_id"));
                int teamId = resultSet.getInt("team_id");
                String teamName = resultSet.getString("team_name");
                String teamColor = resultSet.getString("team_color");
                teams.put(playerId, new PlayerTeam(playerId, teamId, teamName, teamColor));
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getAllTeams...", ex);
        }

        return teams;
    }

    public PlayerTeam getTeam(UUID uuid) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
            String.format("""
                    SELECT t.id AS team_id, t.name AS team_name, t.color AS team_color
                    FROM `%s` AS pt
                    JOIN `%s` AS t ON pt.team_id = t.id
                    WHERE pt.id = UUID_TO_BIN(?);
                """, this.playerTeamTableName, this.teamTableName)
        )) {
            preparedStatement.setString(1, uuid.toString());
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int teamId = resultSet.getInt("team_id");
                String teamName = resultSet.getString("team_name");
                String teamColor = resultSet.getString("team_color");
                return new PlayerTeam(uuid, teamId, teamName, teamColor);
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to getTeam...", ex);
        }

        return null;
    }

    public static final class PlayerTeam {
        private UUID uuid;
        private int teamId;
        private String name;
        private String color;

        public PlayerTeam(UUID uuid, int teamId, String name, String color) {
            this.uuid = uuid;
            this.teamId = teamId;
            this.name = name;
            this.color = color;
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

        public int getTeamId() {
            return teamId;
        }

        public void setTeamId(int teamId) {
            this.teamId = teamId;
        }
    }
}
