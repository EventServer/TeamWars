package eventserver.teamwars.game;

import java.util.TimerTask;

public class StartBattleTask extends TimerTask {
    private final Game game;
    public StartBattleTask(Game game) {
        this.game = game;
    }
    @Override
    public void run() {
        game.setState(Game.State.BATTLE);
        this.cancel();
    }
}
