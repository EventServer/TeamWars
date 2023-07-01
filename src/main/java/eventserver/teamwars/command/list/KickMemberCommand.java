package eventserver.teamwars.command.list;

import eventserver.teamwars.Config;
import eventserver.teamwars.TeamWars;
import eventserver.teamwars.command.SubCommand;
import eventserver.teamwars.game.Game;
import eventserver.teamwars.game.Team;
import eventserver.teamwars.game.TeamMember;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class KickMemberCommand implements SubCommand {
    @Override
    public void onCommand(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage("/teamwars kickmember <teamId> <name>");
            return;
        }

        final Game game = TeamWars.getInstance().getGame();

        final Team team = game.getTeamManager().getTeam(args[1]);

        if (team == null) {
            sender.sendMessage(Config.MESSAGES.NO_TEAM);
            return;
        }

        TeamMember member = team.getMember(args[2]);
        if (member == null) {
            sender.sendMessage(Config.MESSAGES.PLAYER_NO_MEMBER);
            return;
        }

        team.sendMessage(Config.MESSAGES.MEMBER_KICK.replace("%player%", member.getPlayerName()));
        team.kickMember(member);
        if (member.getBukkitInstance() != null) {
            member.getBukkitInstance().teleport(Config.SPAWN);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
