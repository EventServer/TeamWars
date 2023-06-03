package eventserver.teamwars.game;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@AllArgsConstructor @Getter
public class TeamMember {
    private final String playerName;
    @Setter
    private double balance;

    @Getter @Setter
    private Player bukkitInstance = null;

    public JsonObject getJson() {
        JsonObject jo = new JsonObject();
        jo.add("username", new JsonPrimitive(playerName));
        jo.add("balance", new JsonPrimitive(balance));
        return jo;
    }
}
