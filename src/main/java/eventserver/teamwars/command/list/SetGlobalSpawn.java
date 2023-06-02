package eventserver.teamwars.command.list;

import eventserver.teamwars.Config;
import eventserver.teamwars.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SetGlobalSpawn implements SubCommand {
    @Override
    public void onCommand(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (!(sender instanceof Player player))
            return;

        Config.setSpawn(player.getLocation());

        sender.sendMessage(ChatColor.GREEN+"ok");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
