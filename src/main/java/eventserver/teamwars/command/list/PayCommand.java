package eventserver.teamwars.command.list;

import eventserver.teamwars.Config;
import eventserver.teamwars.TeamWars;
import eventserver.teamwars.command.SubCommand;
import eventserver.teamwars.game.Team;
import eventserver.teamwars.game.TeamMember;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PayCommand implements SubCommand {
    @Override
    public void onCommand(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length < 3 || args[1].equalsIgnoreCase(sender.getName())) {
            sender.sendMessage("/tw pay <username> <money>");
            return;
        }

        Team team = TeamWars.getInstance().getGame().getTeamManager().getPlayerTeam(player);
        if (team == null) {
            player.sendMessage(Config.MESSAGES.YOU_NO_TEAM);
            return;
        }
        final TeamMember member = team.getMember(player.getName());
        if (member == null) {
            player.sendMessage(Config.MESSAGES.YOU_NO_TEAM);
            return;
        }

        final String nick = args[1];
        final TeamMember target = team.getMember(nick);
        if (target == null) {
            player.sendMessage(Config.MESSAGES.PLAYER_NO_MEMBER);
            return;
        }

        try {
            final double money = Double.parseDouble(args[2]);
            if (money < 1)
                throw new NumberFormatException();

            if (money > member.getBalance()) {
                player.sendMessage(Config.MESSAGES.NO_BALANCE);
                return;
            }

            member.setBalance(member.getBalance() - money);
            player.sendMessage(Config.MESSAGES.YOU_PAY
                    .replace("%balance%", String.format("%.2f", member.getBalance()))
                    .replace("%target%", target.getPlayerName())
                    .replace("%money%", String.format("%.2f", money)));
            target.setBalance(target.getBalance() + money);

            if (target.getBukkitInstance() != null) {
                target.getBukkitInstance().sendMessage(Config.MESSAGES.YOU_PAY
                        .replace("%balance%", String.format("%.2f", target.getBalance()))
                        .replace("%player%", player.getName())
                        .replace("%money%", String.format("%.2f", money)));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("/tw pay <username> <money>");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();
        final Team team = TeamWars.getInstance().getGame().getTeamManager().getPlayerTeam(player);
        if (team == null)
            return Collections.emptyList();
        List<String> result = new ArrayList<>();
        if (args.length == 2) {
            for (TeamMember member: team.getMembers()) {
                result.add(member.getPlayerName());
            }
        } else if (args.length == 3) {
            TeamMember member = team.getMember(player.getName());
            if (member != null) {
                result.add(String.valueOf(member.getBalance()));
            }
        }
        return result;
    }
}
