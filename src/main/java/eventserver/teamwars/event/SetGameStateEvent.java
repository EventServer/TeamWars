package eventserver.teamwars.event;

import eventserver.teamwars.game.Game;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class SetGameStateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    private final Game game;
    private final Game.State newState;

    public SetGameStateEvent(Game game, Game.State newState) {
        this.game = game;
        this.newState = newState;
    }
}
