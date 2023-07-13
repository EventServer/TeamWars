package eventserver.teamwars.command.list;

import eventserver.teamwars.Config;
import eventserver.teamwars.TeamWars;
import eventserver.teamwars.command.SubCommand;
import eventserver.teamwars.game.Game;
import eventserver.teamwars.game.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AdditionalCommand implements SubCommand {
    @Override
    public void onCommand(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage("/teamwars additional <teamId> <amount>");
            return;
        }

        final Game game = TeamWars.getInstance().getGame();
        final Team team = game.getTeamManager().getTeam(args[1]);

        if (team == null) {
            sender.sendMessage(Config.MESSAGES.NO_TEAM);
            return;
        }

        try {
            final int amount = Integer.parseInt(args[2]);
            team.setAdditionalMembers(amount);
            final String message = Config.MESSAGES.SET_ADDITIONAL
                    .replace("%team%", team.getPrefix())
                    .replace("%team-id%", team.getId())
                    .replace("%amount%", String.valueOf(amount));
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
        } catch (NumberFormatException e) {
            sender.sendMessage("/teamwars additional <teamId> <amount>");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> result = new ArrayList<>();
        if (args.length == 2) {
            for (Team team : TeamWars.getInstance().getGame().getTeamManager().getTeams()) {
                result.add(team.getId());
            }
        } else if (args.length == 3) {
            for (int i = 0; i < 10; i++) {
                result.add(String.valueOf(i));
            }
        }
        return result;
    }
}
