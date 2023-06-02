package eventserver.teamwars.command.list;

import eventserver.teamwars.Config;
import eventserver.teamwars.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SetSlotsCommand implements SubCommand {
    @Override
    public void onCommand(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage("/tw setslots [int]");
            return;
        }
        try {
            int slots = Integer.parseInt(args[1]);
            Config.setSlots(slots);
            sender.sendMessage(ChatColor.GREEN+"ok");
        } catch (NumberFormatException e) {
            sender.sendMessage("/tw setslots [int]");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
