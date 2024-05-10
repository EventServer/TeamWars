package eventserver.teamwars.game;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import eventserver.teamwars.Config;
import eventserver.teamwars.event.MemberDeathEvent;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
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
        this.teams.forEach(Team::clearMembers);
    }

    public void updateRegions() {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Config.world));
        RegionManager netherRegionManager = regionContainer.get(BukkitAdapter.adapt(Config.worldNether));

        if (regionManager == null || netherRegionManager == null)
            return;

        for (final Team team: teams) {
            regionManager.removeRegion(team.getRegion().getRegion().getId());
            regionManager.addRegion(team.getRegion().getRegion());

            netherRegionManager.removeRegion(team.getNetherRegion().getRegion().getId());
            netherRegionManager.addRegion(team.getNetherRegion().getRegion());
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
            switch (game.getState()) {
                case BATTLE -> protectPositionBattleState();
                case ACTIVE -> protectPositionsActiveState();
            }
        }, 40, 40);
    }

    private void protectPositionsActiveState() {
        for (Team team : teams) {
            for (TeamMember member : team.getMembers()) {
                if (member.getBukkitInstance() == null ||
                        isPlayerInsideRegion(Config.world, team.getRegion().getRegion(), member.getBukkitInstance().getLocation()) ||
                        isPlayerInsideRegion(Config.worldNether, team.getNetherRegion().getRegion(), member.getBukkitInstance().getLocation())) {
                    continue;
                }

                Location spawnLocation = team.getRegion().getSpawn();
                Player player = member.getBukkitInstance();

                if (spawnLocation != null && player != null) {
                    team.teleportMember(player, spawnLocation);
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1.5F, 1.5F);
                }
            }
        }
    }

    private void protectPositionBattleState() {
        for (Team team : teams) {
            for (TeamMember member : team.getMembers()) {
                final Player player = member.getBukkitInstance();
                if (player == null) continue;
                final Location location = player.getLocation();
                if (!isInsideAnyTeamRegion(location)) {
                    Team t = getPlayerTeam(player);
                    if (t != null) {
                        t.teleport(t.getRegion().getSpawn());
                    }
                }
            }
        }
    }

    private boolean isInsideAnyTeamRegion(Location location) {
        for (Team team: teams) {
            if (switch (location.getWorld().getEnvironment()) {
                case NETHER -> isPlayerInsideRegion(location.getWorld(), team.getNetherRegion().getRegion(), location);
                case NORMAL -> isPlayerInsideRegion(location.getWorld(), team.getRegion().getRegion(), location);
                default -> false;
            }) return true;
        }
        return false;
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
        private void onDamage(EntityDamageByEntityEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (!(event.getDamager() instanceof Player attacker)) return;
            final Team team = getPlayerTeam(player);
            final Team team2 = getPlayerTeam(attacker);
            if (team == null || team2 == null || team == team2) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        private void onPlayerPortalEvent(PlayerPortalEvent event) {
            final Player player = event.getPlayer();
            final Team team = getPlayerTeam(player);
            if (team == null) return;
            if (game.getState() == Game.State.BATTLE) {
                event.setCancelled(true);
                player.sendMessage(Config.MESSAGES.PORTAL_BATTLE_DENY);
                return;
            }
            if (event.getTo().getWorld().getName().equalsIgnoreCase(Config.worldNether.getName())) {
                event.setCancelled(true);
                team.teleportMember(player, team.getNetherRegion().getSpawn());
            } else if (event.getTo().getWorld().getName().equalsIgnoreCase(Config.world.getName())) {
                event.setCancelled(true);
                team.teleportMember(player, team.getRegion().getSpawn());
            }
        }

        @EventHandler
        private void onPlayerJoin(PlayerJoinEvent event) {
            final Player player = event.getPlayer();

            if (player.hasPlayedBefore()) {
                player.teleport(Config.SPAWN);
                return;
            }

            final Team team = getPlayerTeam(player);
            if (team == null) {
                if (!player.hasPermission("teamwars.admin"))
                    player.teleport(Config.SPAWN);
            } else {
                final TeamMember member = team.getMember(player.getName());
                assert member != null;
                member.setBukkitInstance(player);
                Bukkit.getScheduler().runTaskLater(plugin, () -> team.syncWorldBorder(player), 40);
            }
        }

        @EventHandler
        private void onPlayerDeath(PlayerDeathEvent event) {
            final Player player = event.getEntity();
            final Team team = getPlayerTeam(player);
            if (team == null) return;
            final TeamMember member = team.getMember(player.getName());
            assert member != null;
            if (!member.isLife()) return;

            boolean notify = false;
            if (game.getState() == Game.State.BATTLE) {
                member.setLife(false);
                notify = true;
            }

            member.setDeaths(member.getDeaths() + 1);

            if (member.isLife()) {
                player.setBedSpawnLocation(team.getRegion().getSpawn());
                broadcast(Config.MESSAGES.DEATH_ACTIVE
                        .replace("%balance%", String.format("%.2f", member.getBalance()))
                        .replace("%player%", player.getName())
                        .replace("%team-prefix%", team.getPrefix()));
                member.setBalance(0);
                game.getInventoryReturnManager().onDeath(event);
            } else if (notify) {
                broadcast(Config.MESSAGES.DEATH_BATTLE
                        .replace("%player%", player.getName())
                        .replace("%team-prefix%", team.getPrefix()));
                player.setBedSpawnLocation(Config.SPAWN);
                member.setLife(false);
            }

            final Player killer = event.getEntity().getKiller();
            if (killer != null) {
                Team killerTeam = getPlayerTeam(killer);
                if (killerTeam != null) {
                    final TeamMember killerMember = killerTeam.getMember(player.getName());
                    assert killerMember != null;
                    killerMember.setKills(killerMember.getKills()+1);
                }
            }

            new MemberDeathEvent(team, member, !member.isLife()).callEvent();
        }

//        @EventHandler
//        private void onChunkLoaded(ChunkLoadEvent event) {
//            final Chunk chunk = event.getChunk();
//            if (chunk.getWorld() != Config.world) return;
//            for (Team team: teams) {
//                if (team.getRegion().contains(chunk.getX(), 50, chunk.getZ())) {
//                    chunk.setForceLoaded(true);
//                }
//            }
//        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        private void onPlayerChat(final AsyncPlayerChatEvent event) {
            final Player player = event.getPlayer();
            event.setCancelled(true);
            final Team team = game.getTeamManager().getPlayerTeam(player);
            final String prefix = (team == null) ? ChatColor.GRAY+"■":team.getPrefix();
            if (event.getMessage().startsWith("!")) {
                final String message = Config.CHAT_GLOBAL_FORMAT
                                .replace("%player%", player.getName())
                                .replace("%text%", event.getMessage().substring(1))
                                .replace("%team%", prefix);
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.sendMessage(message);
                });
                return;
            }
            if (team == null) {
                player.sendMessage(Config.MESSAGES.NO_LOCAL_CHAT);
            } else {
                team.sendMessage(player.getName() + ": " + ChatColor.GRAY + event.getMessage());
            }
        }

        private void broadcast(String message) {
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
        }
    }
}
