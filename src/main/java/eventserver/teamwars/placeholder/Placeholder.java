package eventserver.teamwars.placeholder;

import eventserver.teamwars.game.Game;
import eventserver.teamwars.game.Team;
import eventserver.teamwars.game.TeamMember;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholder extends PlaceholderExpansion {
    private final Game game;
    public Placeholder(Game game) {
        this.game = game;
    }
    @Override
    public @NotNull String getIdentifier() {
        return "tw";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Zako";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equalsIgnoreCase("team-prefix")) {
            final Team team = game.getTeamManager().getPlayerTeam(player);
            if (team != null)
                return team.getPrefix();
            return "-";
        }
        if (identifier.equalsIgnoreCase("team-id")) {
            final Team team = game.getTeamManager().getPlayerTeam(player);
            if (team != null)
                return team.getId();
            return "-";
        }
        if (identifier.equalsIgnoreCase("balance")) {
            final Team team = game.getTeamManager().getPlayerTeam(player);
            if (team == null)
                return "0";
            final TeamMember member = team.getMember(player.getName());
            if (member == null)
                return "0";
            return String.format("%.2f",member.getBalance());
        }
        if (identifier.equalsIgnoreCase("team-size")) {
            final Team team = game.getTeamManager().getPlayerTeam(player);
            if (team == null)
                return "-";
            return String.valueOf(team.getMembers().size());
        }
        if (identifier.equalsIgnoreCase("team-full-balance")) {
            final Team team = game.getTeamManager().getPlayerTeam(player);
            if (team == null)
                return "0";
            return String.format("%.2f",team.getFullBalance());
        }
        return "-";
    }
}
