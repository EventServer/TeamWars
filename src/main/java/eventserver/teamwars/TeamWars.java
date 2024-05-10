package eventserver.teamwars;

import eventserver.teamwars.command.SpawnCommand;
import eventserver.teamwars.command.TeamWarsCommand;
import eventserver.teamwars.game.Game;
import eventserver.teamwars.game.TWGame;
import eventserver.teamwars.placeholder.Placeholder;
import eventserver.teamwars.util.BorderManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TeamWars extends JavaPlugin {
    @Getter
    private Game game;
    @Getter
    private static TeamWars instance;
    @Getter
    private BorderManager borderManager;
    private Placeholder placeholder;
    @Override
    public void onEnable() {
        this.instance = this;
        saveDefaultConfig();
        new Config(getConfig());

        this.game = new TWGame(this);
        placeholder = new Placeholder(game);
        borderManager = new BorderManager(this);
        placeholder.register();
        Objects.requireNonNull(getServer().getPluginCommand("teamwars")).setExecutor(new TeamWarsCommand());
        Objects.requireNonNull(getServer().getPluginCommand("spawn")).setExecutor(new SpawnCommand());
    }

    @Override
    public void onDisable() {
        if (placeholder != null)
            placeholder.unregister();
    }
}
