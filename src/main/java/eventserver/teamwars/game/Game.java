package eventserver.teamwars.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import eventserver.teamwars.Config;
import eventserver.teamwars.event.SetGameStateEvent;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

public class Game {
    private StartBattleTask battleTask;
    @Getter
    private final TeamManager teamManager;
    @Getter
    private State state = State.INACTIVE;
    private final JavaPlugin plugin;
    private final Timer startBattleTimer = new Timer();
    @Getter
    private long startBattleDate = 0;

    public Game(JavaPlugin plugin) {
        this.plugin = plugin;
        teamManager = new TeamManager(plugin, plugin.getConfig(), this);
    }

    public JsonObject getJson() {
        JsonObject jo = new JsonObject();
        jo.add("date", new JsonPrimitive(new Date().toString()));
        JsonArray teams = new JsonArray();
        for (Team team: teamManager.getTeams()) {
            teams.add(team.getJson());
        }
        jo.add("teams", teams);
        return jo;
    }

    public void setState(State state) {
        cancelBattleTask();
        new SetGameStateEvent(this, state).callEvent();
        switch (state) {
            case ACTIVE -> {
                startBattleDate = System.currentTimeMillis() + Config.ACTIVE_TIME * 1000L;
                planeStartBattle();
                teamManager.getTeams().forEach(team -> {
                    team.teleport(team.getSpawn());
                });
            } case INACTIVE -> {
                teamManager.getTeams().forEach(team -> {
                    team.teleport(Config.SPAWN);
                });
                saveGameStatistic();
                teamManager.reset();
            } case PREPARATION -> {

            } case BATTLE -> {
                startBattleDate = 0;
                final Title title = Title.title(Component.text(Config.MESSAGES.BATTLE_START_TITLE), Component.text(""));
                for (Team team: teamManager.getTeams()) {
                    team.sendTitle(title);
                }
                for (Team team: teamManager.getTeams()) {
                    team.getRegion().getFlags().put(Flags.ENTRY, StateFlag.State.ALLOW);
                    team.getRegion().getFlags().put(Flags.BUILD, StateFlag.State.ALLOW);
                    team.getRegion().getFlags().put(Flags.USE, StateFlag.State.ALLOW);
                    team.getRegion().getFlags().put(Flags.PVP, StateFlag.State.ALLOW);
                    team.getNetherRegion().getFlags().put(Flags.ENTRY, StateFlag.State.ALLOW);
                    team.getNetherRegion().getFlags().put(Flags.BUILD, StateFlag.State.ALLOW);
                    team.getNetherRegion().getFlags().put(Flags.USE, StateFlag.State.ALLOW);
                    team.getNetherRegion().getFlags().put(Flags.PVP, StateFlag.State.ALLOW);
                }
            }
        }
        this.state = state;
    }

    private StartBattleTask createBattleTask() {
        this.battleTask = new StartBattleTask(this);
        return this.battleTask;
    }

    private void cancelBattleTask() {
        if (this.battleTask != null) {
            this.battleTask.cancel();
            this.battleTask = null;
        }
    }

    public void planeStartBattle() {
        cancelBattleTask();
        startBattleTimer.scheduleAtFixedRate(createBattleTask(),new Date(startBattleDate), Long.MAX_VALUE);
    }

   public void saveGameStatistic() {
       final Date date = new Date();
       final File file = new File(plugin.getDataFolder().getPath()+"/saves/");
       file.mkdirs();
       try {
           DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
           try (final FileWriter writer = new FileWriter(file.getPath()+"/"+df.format(date)+".txt")) {
               writer.write(getJson().toString());
               plugin.getLogger().info("Game statistic has been saved!");
           }
       } catch (IOException e) {
           plugin.getLogger().warning("Error save game statistic.");
           e.printStackTrace();
       }
   }

    public enum State {
        INACTIVE,
        PREPARATION,
        BATTLE,
        ACTIVE;
    }

}
