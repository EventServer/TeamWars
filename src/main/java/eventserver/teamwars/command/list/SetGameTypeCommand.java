package eventserver.teamwars.command.list;

import eventserver.teamwars.TeamWars;
import eventserver.teamwars.command.SubCommand;
import eventserver.teamwars.game.Game;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SetGameTypeCommand implements SubCommand {

    @Override
    public void onCommand(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage("/teamwars setgametype <type>");
            return;
        }

        final Game game = TeamWars.getInstance().getGame();

        try {
            Game.State state = Game.State.valueOf(args[1].toUpperCase());
            if (state == game.getState()) {
                sender.sendMessage("Game state is "+ state.name());
                return;
            }

            game.setState(state);

            sender.sendMessage(ChatColor.GREEN+"Ok");
        } catch (EnumConstantNotPresentException e) {
            sender.sendMessage("Invalid game state");
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> result = new ArrayList<>();
        for (Game.State state: Game.State.values()) {
            result.add(state.name());
        }
        return result;
    }
}
