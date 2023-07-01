package eventserver.teamwars.game;

import eventserver.teamwars.TeamWars;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TopMoneyService {
    private final Team team;
    private final List<TeamMember> members = new ArrayList<>();
    public TopMoneyService(Team team) {
        this.team = team;
        Bukkit.getScheduler().runTaskTimerAsynchronously(TeamWars.getInstance(), this::sort, 0, 100);
    }

    private void sort() {
        final List<TeamMember> sorted = new ArrayList<>(team.getMembers());
        sorted.sort(Comparator.comparingDouble(TeamMember::getBalance).reversed());
        members.clear();
        members.addAll(sorted);
    }

    public TeamMember get(int place) {;
        if (members.size() < place)
            return null;
        return members.get(place-1);
    }
}
