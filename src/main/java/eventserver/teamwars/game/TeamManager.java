package eventserver.teamwars.game;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import eventserver.teamwars.Config;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class TeamManager {
    @Getter
    private final Set<Team> teams = new HashSet<>();
    private final JavaPlugin plugin;

    public TeamManager(JavaPlugin plugin, FileConfiguration file) {
        this.plugin = plugin;
        final ConfigurationSection teamsSection = file.getConfigurationSection("teams");
        if (teamsSection == null) {
            plugin.getLogger().warning("Invalid config");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        this.teams.addAll(Config.parseTeams(teamsSection));

        updateRegions();

        plugin.getServer().getPluginManager().registerEvents(new BukkitListener(), plugin);
    }

    public @Nullable Team getPlayerTeam(Player player) {
        for (Team team: teams) {
            if (team.isMember(player.getName())) {
                return team;
            }
        }
        return null;
    }

    public void updateRegions() {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Config.world));

        if (regionManager == null)
            return;

        for (final Team team: teams) {
            regionManager.removeRegion(team.getRegion().getId());
            regionManager.addRegion(team.getRegion());
        }
    }

    public @Nullable Team getTeam(String id) {
        for (Team team: teams) {
            if (team.getId().equalsIgnoreCase(id))
                return team;
        }
        return null;
    }

    private class BukkitListener implements Listener {
        @EventHandler
        private void onPluginDisabled(PluginDisableEvent event) {
            if (event.getPlugin() == plugin) {
                teams.forEach(Config::saveMembers);
            }
        }

        @EventHandler
        private void onPlayerLeave(PlayerQuitEvent event) {
            final Player player = event.getPlayer();

            for (Team team: teams) {
                final TeamMember member = team.getMember(player.getName());
                if (member == null) continue;
                member.setBukkitInstance(null);
            }
        }

        @EventHandler
        private void onPlayerJoin(PlayerJoinEvent event) {
            final Player player = event.getPlayer();

            if (player.hasPlayedBefore()) {
                player.teleport(Config.SPAWN);
            }

            for (Team team: teams) {
                final TeamMember member = team.getMember(player.getName());
                if (member == null) continue;
                member.setBukkitInstance(player);
            }
        }

        @EventHandler
        private void onPlayerChat(final AsyncPlayerChatEvent event) {
            if (event.getMessage().startsWith("!")) return;
            final Player player = event.getPlayer();
            final Team team = getPlayerTeam(player);
            if (team == null) return;
            team.sendMessage(player.getName()+": "+ ChatColor.GRAY+event.getMessage());
            event.setCancelled(true);
        }
    }
}
