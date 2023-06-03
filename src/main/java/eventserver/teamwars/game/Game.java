package eventserver.teamwars.game;

import eventserver.teamwars.Config;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class Game {
    @Getter
    private final TeamManager teamManager;
    @Getter
    private State state = State.INACTIVE;

    public Game(JavaPlugin plugin) {
        teamManager = new TeamManager(plugin, plugin.getConfig());
    }

    public void setState(State state) {
        switch (state) {
            case ACTIVE -> {
                teamManager.getTeams().forEach(team -> {
                    team.teleport(team.getSpawn());
                });
            } case INACTIVE -> {
                teamManager.getTeams().forEach(team -> {
                    team.teleport(Config.SPAWN);
                });
            }
        }
        this.state = state;
    }

    public enum State {
        INACTIVE,
        PREPARATION,
        ACTIVE;
    }

}
