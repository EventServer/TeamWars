package eventserver.teamwars;

import eventserver.teamwars.command.SpawnCommand;
import eventserver.teamwars.command.TeamWarsCommand;
import eventserver.teamwars.game.Game;
import eventserver.teamwars.placeholder.Placeholder;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TeamWars extends JavaPlugin {
    @Getter
    private Game game;
    @Getter
    private static TeamWars instance;
    private Placeholder placeholder;
    @Override
    public void onEnable() {
        this.instance = this;
        saveDefaultConfig();
        new Config(getConfig());

        this.game = new Game(this);
        placeholder = new Placeholder(game);
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
