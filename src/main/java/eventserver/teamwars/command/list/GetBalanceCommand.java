package eventserver.teamwars.command.list;

import eventserver.teamwars.TeamWars;
import eventserver.teamwars.command.SubCommand;
import eventserver.teamwars.game.Game;
import eventserver.teamwars.game.Team;
import eventserver.teamwars.game.TeamMember;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class GetBalanceCommand implements SubCommand {
    @Override
    public void onCommand(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage("/tw setbalance [name] [balance]");
            return;
        }
        final String name = args[1];
        final Game game = TeamWars.getInstance().getGame();
        for (Team team : game.getTeamManager().getTeams()) {
            final TeamMember member = team.getMember(name);
            if (member != null) {
                sender.sendMessage(String.valueOf(member.getBalance()));
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
