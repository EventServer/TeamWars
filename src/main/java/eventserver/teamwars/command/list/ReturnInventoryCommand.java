package eventserver.teamwars.command.list;

import eventserver.teamwars.Config;
import eventserver.teamwars.TeamWars;
import eventserver.teamwars.command.SubCommand;
import eventserver.teamwars.game.Game;
import eventserver.teamwars.game.Team;
import eventserver.teamwars.game.TeamMember;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ReturnInventoryCommand implements SubCommand {
    @Override
    public void onCommand(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (!(sender instanceof Player player)) return;
        final Game game = TeamWars.getInstance().getGame();
        Team team = game.getTeamManager().getPlayerTeam(player);
        if (team == null) {
            player.sendMessage(Config.MESSAGES.YOU_NO_TEAM);
            return;
        }

        if (!game.getInventoryReturnManager().isContains(player)) {
            player.sendMessage(Config.MESSAGES.NO_KEEP_INVENTORY);
            return;
        }

        TeamMember member = team.getMember(player.getName());
        assert member != null;
        if (member.getBalance() < Config.INVENTORY_RETURN_PRICE) {
            player.sendMessage(Config.MESSAGES.NO_BALANCE);
            return;
        }

        member.setBalance(member.getBalance() - Config.INVENTORY_RETURN_PRICE);

        game.getInventoryReturnManager().returnInventory(player);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
