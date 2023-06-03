package eventserver.teamwars.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import eventserver.teamwars.Config;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Game {
    @Getter
    private final TeamManager teamManager;
    @Getter
    private State state = State.INACTIVE;
    private final JavaPlugin plugin;

    public Game(JavaPlugin plugin) {
        this.plugin = plugin;
        teamManager = new TeamManager(plugin, plugin.getConfig());
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
        switch (state) {
            case ACTIVE -> {
                teamManager.getTeams().forEach(team -> {
                    team.teleport(team.getSpawn());
                });
            } case INACTIVE -> {
                teamManager.getTeams().forEach(team -> {
                    team.teleport(Config.SPAWN);
                });
                saveGameStatistic();
                teamManager.reset();
            }
        }
        this.state = state;
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
        ACTIVE;
    }

}
