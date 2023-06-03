package eventserver.teamwars.command;

import eventserver.teamwars.Config;
import eventserver.teamwars.TeamWars;
import eventserver.teamwars.game.Game;
import eventserver.teamwars.game.Team;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        final Game game = TeamWars.getInstance().getGame();
        final Team team = game.getTeamManager().getPlayerTeam(player);
        if (team != null && game.getState() == Game.State.ACTIVE) {
            sender.sendMessage(Config.MESSAGES.NO_SPAWN_TELEPORT);
            return true;
        }

        player.teleport(Config.SPAWN);

        return true;
    }
}
