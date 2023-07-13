package eventserver.teamwars.game;

import com.google.gson.JsonObject;

public interface Game {
    JsonObject getJson();

    /**
     * Переключить состояние игры
     * @param state
     */

    void setState(State state);

    StartBattleTask createBattleTask();

    void cancelBattleTask();


    /**
     * Сохранить текущее состояние игры
     * в файл
     */

    void saveGameStatistic();

    TeamManager getTeamManager();

    /**
     * Текущее состояние игры
     * @return
     */

    State getState();


    InventoryReturnManager getInventoryReturnManager();

    /**
     * Возвращает unixtime дату и время
     * автоматического начала битвы
     * @return
     */

    long getStartBattleDate();
    void setStartBattleDate(long date);


    public enum State {
        INACTIVE,
        PREPARATION,
        BATTLE,
        ACTIVE;
    }
}
