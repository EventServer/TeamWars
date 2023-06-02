package eventserver.teamwars.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@AllArgsConstructor @Getter
public class TeamMember {
    private final String playerName;
    private double balance;

    @Getter @Setter
    private Player bukkitInstance = null;


}
