package eventserver.teamwars.command.list;

import eventserver.teamwars.TeamWars;
import eventserver.teamwars.command.SubCommand;
import eventserver.teamwars.game.Game;
import eventserver.teamwars.game.Team;
import eventserver.teamwars.game.TeamManager;
import eventserver.teamwars.game.region.TeamRegion;
import eventserver.teamwars.util.BorderManager;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
public class SetCenterGameCommand implements SubCommand {

    public void onCommand(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length < 2) {
            sender.sendMessage("/tw setcentergame <team-size>");
            return;
        }
        final int size = getInt(args[1]);
        if (size <= 0) {
            sender.sendMessage("size <= 0");
            return;
        }

        final World world = player.getWorld();
        final Location l = player.getLocation();
        TeamManager teamManager = TeamWars.getInstance().getGame().getTeamManager();
        final var teams = teamManager.getTeams();

        int i = -1;
        for (Team team: teams) {
            i++;
            TeamRegion region = switch (world.getEnvironment()) {
                case NORMAL -> team.getRegion();
                case NETHER -> team.getNetherRegion();
                default -> throw new IllegalStateException("Unexpected world point: " + world.getName());
            };

            if (region == null) {
                continue;
            }

            double x = l.getX() + size*i;
            double z = l.getZ();

            final var min = region.getMin();
            final var max = region.getMax();

            min.setX(x+size);
            min.setZ(z+size);
            min.setY(-100);

            max.setX(x);
            max.setZ(z);
            max.setY(300);

            region.setPoints(min, max);

            team.syncMembersBorder();
        }
        TeamWars.getInstance().saveConfig();
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }

    public static int getInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
