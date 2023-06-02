package eventserver.teamwars.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SubCommand {
    void onCommand(@NonNull CommandSender sender, @NonNull String[] args);
    List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args);
}
