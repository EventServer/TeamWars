package eventserver.teamwars.command.list;

import eventserver.teamwars.Config;
import eventserver.teamwars.TeamWars;
import eventserver.teamwars.command.SubCommand;
import eventserver.teamwars.game.Game;
import eventserver.teamwars.game.Team;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JoinCommand implements SubCommand {
    @Override
    public void onCommand(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length < 2) {
            sender.sendMessage("/teamwars join <teamId>");
            return;
        }

        final Game game = TeamWars.getInstance().getGame();
        if (game.getState() != Game.State.PREPARATION) {
            sender.sendMessage(Config.MESSAGES.NO_JOIN_NO_PREPARE);
            return;
        }

        final Team team = game.getTeamManager().getTeam(args[1]);

        if (team == null) {
            sender.sendMessage(Config.MESSAGES.NO_TEAM);
            return;
        }

        if (game.getTeamManager().getPlayerTeam(player) != null) {
            player.sendMessage(Config.MESSAGES.YOU_TEAM_MEMBER);
            return;
        }

        if (team.getMembers().size() >= Config.TEAMS_MAX_SLOTS) {
            player.sendMessage(Config.MESSAGES.TEAM_FULL);
            return;
        }

        team.addMember(player);
        team.sendMessage(Config.MESSAGES.PLAYER_TEAM_JOIN.replace("%player%", player.getName()));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> result = new ArrayList<>();
        for (Team team: TeamWars.getInstance().getGame().getTeamManager().getTeams()) {
            result.add(team.getId());
        }
        return result;
    }
}
