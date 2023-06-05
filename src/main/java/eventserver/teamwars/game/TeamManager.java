package eventserver.teamwars.game;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import eventserver.teamwars.Config;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
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
    private final Game game;

    public TeamManager(JavaPlugin plugin, FileConfiguration file, Game game) {
        this.game = game;
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

        runPositionControlScheduler();
    }

    public @Nullable Team getPlayerTeam(Player player) {
        for (Team team: teams) {
            if (team.isMember(player.getName())) {
                return team;
            }
        }
        return null;
    }

    public void reset() {
        this.teams.forEach(team -> {
            team.getMembers().clear();
        });
    }

    public void updateRegions() {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Config.world));
        RegionManager netherRegionManager = regionContainer.get(BukkitAdapter.adapt(Config.worldNether));

        if (regionManager == null || netherRegionManager == null)
            return;

        for (final Team team: teams) {
            regionManager.removeRegion(team.getRegion().getId());
            regionManager.addRegion(team.getRegion());

            netherRegionManager.removeRegion(team.getNetherRegion().getId());
            netherRegionManager.addRegion(team.getNetherRegion());
        }
    }

    public @Nullable Team getTeam(String id) {
        for (Team team: teams) {
            if (team.getId().equalsIgnoreCase(id))
                return team;
        }
        return null;
    }

    public void runPositionControlScheduler() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (game.getState() != Game.State.ACTIVE) return;
            for (Team team: teams) {
                for (TeamMember member: team.getMembers()) {
                    if (member.getBukkitInstance() == null
                            || isPlayerInsideRegion(Config.world, team.getRegion(), member.getBukkitInstance().getLocation())
                            || isPlayerInsideRegion(Config.worldNether, team.getNetherRegion(), member.getBukkitInstance().getLocation()))
                        continue;

                    member.getBukkitInstance().teleport(team.getSpawn());
                    member.getBukkitInstance()
                            .playSound(member.getBukkitInstance().getLocation(),
                                    Sound.ENTITY_SHULKER_TELEPORT, 1.5F, 1.5F);
                }
            }
        }, 40, 40);
    }

    private boolean isPlayerInsideRegion(World world, ProtectedCuboidRegion region, Location playerLocation) {
        if (!world.getName().equalsIgnoreCase(playerLocation.getWorld().getName()))
            return false;
        return region.contains(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ());
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
        private void onPlayerPortalEvent(PlayerPortalEvent event) {
            final Player player = event.getPlayer();
            final Team team = getPlayerTeam(player);
            if (team == null) return;
            if (event.getTo().getWorld().getName().equalsIgnoreCase(Config.worldNether.getName())) {
                event.setCancelled(true);
                player.teleport(team.getNetherSpawn());
            } else if (event.getTo().getWorld().getName().equalsIgnoreCase(Config.world.getName())) {
                event.setCancelled(true);
                player.teleport(team.getSpawn());
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
        private void onPlayerDeath(PlayerDeathEvent event) {
            final Player player = event.getPlayer();
            final Team team = getPlayerTeam(player);
            if (team == null) return;
            final TeamMember member = team.getMember(player.getName());
            assert member != null;
            if (game.getState() == Game.State.BATTLE) {
                member.setLife(false);
            }
            if (member.isLife()) {
                player.setBedSpawnLocation(team.getSpawn());
                broadcast(Config.MESSAGES.DEATH_ACTIVE
                        .replace("%balance%", String.format("%.2f", member.getBalance()))
                        .replace("%player%", player.getName())
                        .replace("%team-prefix%", team.getPrefix()));
                member.setBalance(0);
            } else {
                broadcast(Config.MESSAGES.DEATH_BATTLE
                        .replace("%player%", player.getName())
                        .replace("%team-prefix%", team.getPrefix()));
                player.setBedSpawnLocation(Config.SPAWN);
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

        private void broadcast(String message) {
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
        }
    }
}
