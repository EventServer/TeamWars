package eventserver.teamwars.game;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.entity.Player;

@Setter
@AllArgsConstructor @Getter @ToString
public class TeamMember {
    private final String playerName;
    private boolean life;

    private double balance;
    private int kills;
    private int deaths;

    @Getter @Setter
    private Player bukkitInstance = null;

    public boolean isActive() {
        return  (bukkitInstance != null && isLife());
    }

    public JsonObject getJson() {
        JsonObject jo = new JsonObject();
        jo.add("username", new JsonPrimitive(playerName));
        jo.add("balance", new JsonPrimitive(balance));
        jo.add("life", new JsonPrimitive(life));
        jo.add("kills", new JsonPrimitive(kills));
        jo.add("deaths", new JsonPrimitive(deaths));
        return jo;
    }
}
