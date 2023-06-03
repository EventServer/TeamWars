package eventserver.teamwars.command;

import eventserver.teamwars.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeamWarsCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) return true;
        final String first_argument = args[0].toLowerCase();
        for (final SubCommands subCommands : SubCommands.values()) {
            for (final String crateArgs : subCommands.getAliases()) {
                if (crateArgs.equalsIgnoreCase(first_argument)) {
                    if (subCommands.getPermission() == null || sender.hasPermission(subCommands.getPermission())){
                        subCommands.getCommand().onCommand(sender,args);
                    } else {
                        sender.sendMessage(Config.MESSAGES.NO_PERMISSION);
                    }
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            ArrayList<String> tabs = new ArrayList<>();
            for (SubCommands cmd : SubCommands.values()) {
                if (cmd.getPermission() == null || sender.hasPermission(cmd.getPermission()))
                    tabs.add(cmd.getAliases()[0]);
            }
            return tabs;
        }
        final String first_argument = args[0].toLowerCase();
        for (final SubCommands subCommands : SubCommands.values()) {
            for (final String crateArgs : subCommands.getAliases()) {
                if (crateArgs.equalsIgnoreCase(first_argument)) {
                    return subCommands.getCommand().onTabComplete(sender, command, alias, args);
                }
            }
        }

        return Collections.emptyList();
    }
}
