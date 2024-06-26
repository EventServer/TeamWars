package eventserver.teamwars;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import eventserver.teamwars.game.Team;
import eventserver.teamwars.game.TeamMember;
import eventserver.teamwars.game.region.TeamRegion;
import eventserver.teamwars.gui.TeamGuiElement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Config {
    private static FileConfiguration file;
    public Config(FileConfiguration file) {
        this.file = file;
        ConfigurationSection messagesSection = file.getConfigurationSection("messages");
        if (messagesSection == null) {
            TeamWars.getInstance().getLogger().warning("Invalid config, no messages secttion");
            TeamWars.getInstance().getServer().getPluginManager().disablePlugin(TeamWars.getInstance());
            return;
        }

        parseMessages(messagesSection);
        world = Bukkit.getWorld(file.getString("world", "world"));
        worldNether = Bukkit.getWorld(file.getString("nether-world", "world_nether"));
        TeamWars.getInstance().getLogger().info("World: "+ world+", Nether: "+worldNether);

        List<?> objects = file.getList("flags");
        final FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        for (Object o : objects) {
            LinkedHashMap lhm = (LinkedHashMap) o;
            StateFlag.State state = StateFlag.State.valueOf((String) lhm.get("state"));
            Flag flag = Flags.fuzzyMatchFlag(flagRegistry, (String) lhm.get("flag"));

            flags.put(flag, state);
        }

        TEAMS_MAX_SLOTS = file.getInt("teams-max-slots", 50);
        ACTIVE_TIME = file.getInt("active-time", 3600);

        ConfigurationSection spawnLocation = file.getConfigurationSection("spawn-location");
        if (spawnLocation != null)
            SPAWN = parseLocation(spawnLocation);

        CHAT_GLOBAL_FORMAT = file.getString("chat-global-format");

        INVENTORY_RETURN_PRICE = file.getInt("inventory-return-price", 350);
    }

    public static void parseMessages(ConfigurationSection section) {
        MESSAGES.NO_PERMISSION = section.getString("no-permission");
        MESSAGES.NO_JOIN_NO_PREPARE = section.getString("no-join-no-prepare");
        MESSAGES.NO_TEAM = section.getString("no-team");

        MESSAGES.YOU_TEAM_MEMBER = section.getString("you-team-member");
        MESSAGES.PLAYER_TEAM_JOIN = section.getString("player-team-join");
        MESSAGES.TELEPORT_OK = section.getString("teleport-ok");
        MESSAGES.TEAM_FULL = section.getString("team-full");
        MESSAGES.PLAYER_NO_MEMBER = section.getString("player-no-member");
        MESSAGES.MEMBER_KICK = section.getString("member-kick");
        MESSAGES.NO_SPAWN_TELEPORT = section.getString("no-spawn-teleport");
        MESSAGES.YOU_NO_TEAM = section.getString("you-no-team");
        MESSAGES.NO_BALANCE = section.getString("no-balance");
        MESSAGES.SET_ADDITIONAL = section.getString("set-additional");
        MESSAGES.YOU_PAY = section.getString("you-pay");
        MESSAGES.WHERE_PAY = section.getString("where-pay");
        MESSAGES.TIME_FORMAT = section.getString("time-format", "%h%ч. %m%м. %s%с.");
        MESSAGES.BATTLE_START_TITLE = section.getString("battle-start-title");
        MESSAGES.DEATH_ACTIVE = section.getString("death-active");
        MESSAGES.PORTAL_BATTLE_DENY = section.getString("portal-battle-deny");
        MESSAGES.DEATH_BATTLE = section.getString("death-battle");
        MESSAGES.PREPARE_START = section.getStringList("prepare-start");
        MESSAGES.NO_LOCAL_CHAT = section.getString("no-local-chat");
        MESSAGES.LEAVE_TEAM = section.getString("leave-team");
        MESSAGES.KEEP_INVENTORY_NOTIFY = section.getStringList("keep-inventory-notify");
        MESSAGES.NO_KEEP_INVENTORY = section.getString("no-keep-inventory");
    }

    public static int INVENTORY_RETURN_PRICE;
    public static String CHAT_GLOBAL_FORMAT;

    public static class MESSAGES {
        public static String NO_PERMISSION;
        public static String YOU_PAY;
        public static String WHERE_PAY;
        public static String NO_KEEP_INVENTORY;
        public static String NO_BALANCE;
        public static List<String> PREPARE_START;
        public static String SET_ADDITIONAL;
        public static String YOU_NO_TEAM;
        public static String PORTAL_BATTLE_DENY;
        public static String TIME_FORMAT;
        public static String NO_LOCAL_CHAT;
        public static List<String> KEEP_INVENTORY_NOTIFY;
        public static String MEMBER_KICK;
        public static String NO_JOIN_NO_PREPARE;
        public static String YOU_TEAM_MEMBER;
        public static String LEAVE_TEAM;
        public static String NO_TEAM;
        public static String PLAYER_NO_MEMBER;
        public static String PLAYER_TEAM_JOIN;
        public static String TELEPORT_OK;
        public static String DEATH_ACTIVE;
        public static String DEATH_BATTLE;
        public static String NO_SPAWN_TELEPORT;
        public static String TEAM_FULL;
        public static String BATTLE_START_TITLE;
    }

    public static void setSlots(int slots) {
        TEAMS_MAX_SLOTS = slots;
        file.set("teams-max-slots", slots);
        TeamWars.getInstance().saveConfig();
    }

    public static void setSpawn(Location location) {
        Map<String, Object> l = new LinkedHashMap<>();
        l.put("x", location.getBlockX());
        l.put("z", location.getBlockZ());
        l.put("y", location.getBlockY());
        l.put("world", location.getWorld().getName());

        file.createSection("spawn-location", l);
        TeamWars.getInstance().saveConfig();
    }

    public static Location SPAWN;
    public static int TEAMS_MAX_SLOTS;
    private static final Map<Flag, StateFlag.State> flags = new HashMap<>();
    public static World worldNether;
    public static World world;

    public static Set<Team> parseTeams(ConfigurationSection section) {
        final Set<Team> result = new HashSet<>();

        for (final String id: section.getKeys(false)) {
            final ConfigurationSection teamSection = section.getConfigurationSection(id);
            if (teamSection != null) {
                final Team team = parseTeam(id, teamSection);
                if (team != null)
                    result.add(team);
            }
        }

        return result;
    }

    public static int ACTIVE_TIME;
    public static @Nullable Team parseTeam(final String id, ConfigurationSection section) {
        final ConfigurationSection regionSection = section.getConfigurationSection("region");
        if (regionSection == null) {
            TeamWars.getInstance().getLogger().warning("Team "+id+" region section not defined");
            return null;
        }
        final ConfigurationSection netherRegionSection = section.getConfigurationSection("nether-region");
        if (netherRegionSection == null) {
            TeamWars.getInstance().getLogger().warning("Team "+id+" nether region section not defined");
            return null;
        }
        final ConfigurationSection guiSection = section.getConfigurationSection("item-element");
        if (guiSection == null) {
            TeamWars.getInstance().getLogger().warning("Team "+id+" gui section not defined");
            return null;
        }
        final ConfigurationSection spawnLocation = section.getConfigurationSection("spawn-point");
        if (spawnLocation == null) {
            TeamWars.getInstance().getLogger().warning("Team "+id+" spawn section not defined");
            return null;
        }
        final ConfigurationSection netherSpawnLocation = section.getConfigurationSection("nether-spawn-point");
        if (netherSpawnLocation == null) {
            TeamWars.getInstance().getLogger().warning("Team "+id+" nether spawn section not defined");
            return null;
        }
        final String prefix = section.getString("prefix", "");
        final TeamRegion region = new TeamRegion(world, regionSection);
        region.createWgRegion(id);
        final TeamRegion netherRegion = new TeamRegion(worldNether, netherRegionSection);
        netherRegion.createWgRegion(id);
        final TeamGuiElement guiElement = parseGuiElement(guiSection);
//        final Location spawn = parseLocation(world, spawnLocation);
//        final Location netherSpawn = parseLocation(worldNether, netherSpawnLocation);

        flags.forEach((f, s) -> region.getRegion().getFlags().put(f, s));

        Set<TeamMember> members;

        final ConfigurationSection membersSection = section.getConfigurationSection("members");
        if (membersSection == null)
            members = Collections.emptySet();
        else
            members = parseMembersTeam(membersSection);

        return new Team(id, guiElement, region, netherRegion, members, prefix);
    }

    public static Set<TeamMember> parseMembersTeam(ConfigurationSection section) {
        final Set<TeamMember> result = new HashSet<>();
        for (String username: section.getKeys(false)) {
            final ConfigurationSection memberSection = section.getConfigurationSection(username);
            if (memberSection == null)
                continue;
            final double balance = memberSection.getDouble("balance", 0D);
            final boolean life = memberSection.getBoolean("life", true);
            final int deaths = memberSection.getInt("deaths", 0);
            final int kills = memberSection.getInt("kills", 0);

            result.add(new TeamMember(username, life, balance, kills, deaths, Bukkit.getPlayer(username)));
        }

        return result;
    }

    public static Location parseLocation(World world, ConfigurationSection section) {
        final double x = section.getDouble("x");
        final double y = section.getDouble("y");
        final double z = section.getDouble("z");
        return new Location(world, x, y, z);
    }

    public static Location parseLocation(ConfigurationSection section) {
        final double x = section.getDouble("x");
        final double y = section.getDouble("y");
        final double z = section.getDouble("z");
        final String worldName = section.getString("world");
        final World w = Bukkit.getWorld(worldName);
        return new Location(w, x, y, z);
    }

    public static void saveMembers(Team team) {
        LinkedHashMap<String, Map<String, Object>> membersMap = new LinkedHashMap<>();
        team.getMembers().forEach(member -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("balance", member.getBalance());
            if (!member.isLife())
                map.put("life", false);
            membersMap.put(member.getPlayerName(), map);
        });
        file.createSection("teams."+team.getId()+".members", membersMap);
        TeamWars.getInstance().saveConfig();
    }

    public static TeamGuiElement parseGuiElement(ConfigurationSection section) {
        final String display = section.getString("display");
        final int slot = section.getInt("slot");
        final Material material = Material.getMaterial(section.getString("material", "STONE").toUpperCase());
        final List<String> lore = section.getStringList("lore");

        return new TeamGuiElement(slot, display, material, lore);
    }
}
