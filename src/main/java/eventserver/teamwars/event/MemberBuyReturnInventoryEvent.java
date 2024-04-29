package eventserver.teamwars.event;

import eventserver.teamwars.game.Team;
import eventserver.teamwars.game.TeamMember;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class MemberBuyReturnInventoryEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    private final TeamMember member;
    @Setter
    private double price;
    private final Team team;

    public MemberBuyReturnInventoryEvent(Team team, TeamMember member, double price) {
        this.team = team;
        this.price = price;
        this.member = member;
    }
}
