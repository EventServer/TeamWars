package eventserver.teamwars.event;

import eventserver.teamwars.game.Team;
import eventserver.teamwars.game.TeamMember;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MemberDeathEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Getter
    private final TeamMember member;
    @Getter
    private final boolean finalDeath;
    @Getter
    private final Team team;

    public MemberDeathEvent(Team team, TeamMember member, boolean finalDeath) {
        this.team = team;
        this.finalDeath = finalDeath;
        this.member = member;
    }
}
