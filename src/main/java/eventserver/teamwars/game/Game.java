package eventserver.teamwars.game;

import com.google.gson.JsonObject;

public interface Game {
    JsonObject getJson();

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

    /**
     * Возвращает количество игроков,
     * которое может вступить в команду
     * прямо сейчас
     * @return
     */
    int getAdditionalMembers();

    InventoryReturnManager getInventoryReturnManager();

    /**
     * Возвращает unixtime дату и время
     * автоматического начала битвы
     * @return
     */

    long getStartBattleDate();

    /**
     * Устанавливает количество игроков,
     * которые смогут вступить в команду прямо сейчас
     * @param additionalMembers
     */

    void setAdditionalMembers(int additionalMembers);

    public enum State {
        INACTIVE,
        PREPARATION,
        BATTLE,
        ACTIVE;
    }
}
