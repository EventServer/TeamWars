package eventserver.teamwars.placeholder;

import eventserver.teamwars.Config;
import eventserver.teamwars.game.Game;
import eventserver.teamwars.game.Team;
import eventserver.teamwars.game.TeamMember;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (identifier.equalsIgnoreCase("slots")) {
            return String.valueOf(Config.TEAMS_MAX_SLOTS);
        }
        if (identifier.equalsIgnoreCase("time")) {
            long date = game.getStartBattleDate() - System.currentTimeMillis();
            if (date < 1) {
                return "- - -";
            }
            long hours = TimeUnit.MILLISECONDS.toHours(date);
            long min = TimeUnit.MILLISECONDS.toMinutes(date) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(date));
            long sec = TimeUnit.MILLISECONDS.toSeconds(date) - min*60 - hours * 3600;
            return Config.MESSAGES.TIME_FORMAT
                    .replace("%h%", String.valueOf(hours))
                    .replace("%m%",  String.valueOf(min))
                    .replace("%s%", String.valueOf(sec));
        }
        if (identifier.startsWith("team-prefix-")) {
            String id = identifier.replace("team-prefix-", "");
            final Team team = game.getTeamManager().getTeam(id);
            if (team == null) return "";
            return team.getPrefix();
        }
        if (identifier.startsWith("team-balance-")) {
            String id = identifier.replace("team-balance-", "");
            final Team team = game.getTeamManager().getTeam(id);
            if (team == null) return "0";
            return String.format("%.2f", team.getFullBalance());
        }
        if (identifier.startsWith("team-active-members-")) {
            String id = identifier.replace("team-active-members-", "");
            final Team team = game.getTeamManager().getTeam(id);
            if (team == null) return "0";
            return String.valueOf(team.getActiveMembersCount());
        }
        Pattern pattern = Pattern.compile("top-money-name-([A-Za-z0-9]+)-(\\d+)");
        Matcher matcher = pattern.matcher(identifier);
        if (matcher.matches()) {
            String teamId = matcher.group(1);
            int place = Integer.parseInt(matcher.group(2));
            Team team = game.getTeamManager().getTeam(teamId);
            if (team == null)
                return "- - -";
            TeamMember member = team.getTopMoneyService().get(place);
            if (member == null)
                return "- - -";
            return member.getPlayerName();
        }

        pattern = Pattern.compile("top-money-balance-([A-Za-z0-9]+)-(\\d+)");
        matcher = pattern.matcher(identifier);
        if (matcher.matches()) {
            String teamId = matcher.group(1);
            int place = Integer.parseInt(matcher.group(2));
            Team team = game.getTeamManager().getTeam(teamId);
            if (team == null)
                return "- - -";
            TeamMember member = team.getTopMoneyService().get(place);
            if (member == null)
                return "- - -";
            return String.format("%,.0f", member.getBalance()).replace(",", ".");
        }
        if (player == null) return "";
        if (identifier.equalsIgnoreCase("team-prefix")) {
            final Team team = game.getTeamManager().getPlayerTeam(player);
            if (team != null)
                return team.getPrefix();
            return ChatColor.GRAY+"■ ";
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
        if (identifier.equalsIgnoreCase("kills")) {
            final Team team = game.getTeamManager().getPlayerTeam(player);
            if (team == null)
                return "0";
            final TeamMember member = team.getMember(player.getName());
            if (member == null)
                return "0";
            return String.valueOf(member.getKills());
        }
        if (identifier.equalsIgnoreCase("deaths")) {
            final Team team = game.getTeamManager().getPlayerTeam(player);
            if (team == null)
                return "0";
            final TeamMember member = team.getMember(player.getName());
            if (member == null)
                return "0";
            return String.valueOf(member.getDeaths());
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
