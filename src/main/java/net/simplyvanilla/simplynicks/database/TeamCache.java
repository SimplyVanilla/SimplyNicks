package net.simplyvanilla.simplynicks.database;

import net.simplyvanilla.simplynicks.SimplyNicks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class TeamCache {
    private final SimplyNicks plugin;
    private final Map<UUID, TeamMySQL.PlayerTeam> teams = new HashMap<>();

    public TeamCache(SimplyNicks plugin) {
        this.plugin = plugin;
    }

    public void initCache() {
        for (var entry : this.plugin.getTeamDatabase().getAllTeams().entrySet()) {
            addTeam(entry.getKey(), entry.getValue());
        }
    }

    public boolean isTeamExists(String name) {
        return this.teams.values().stream().anyMatch(team -> team.getName().equals(name));
    }

    public void addTeam(UUID key, TeamMySQL.PlayerTeam value) {
        this.teams.put(key, value);
    }

    public TeamMySQL.PlayerTeam getTeam(UUID uuid) {
        return this.teams.get(uuid);
    }

    public void removeTeam(UUID uuid) {
        this.teams.remove(uuid);
    }

    public void removeTeamByName(String name) {
        this.teams.values().removeIf(team -> team.getName().equals(name));
    }

    public void updateTeam(String name, Consumer<TeamMySQL.PlayerTeam> consumer) {
        this.teams.values().stream().filter(team -> team.getName().equals(name)).forEach(consumer);
    }
}
