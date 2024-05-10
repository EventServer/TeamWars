package eventserver.teamwars.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import eventserver.teamwars.TeamWars;
import eventserver.teamwars.game.region.TeamRegion;
import eventserver.teamwars.gui.TeamGuiElement;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

@Getter
public class Team {
    private final Set<TeamMember> members;
    private final String id;
    private final TeamGuiElement guiElement;
    private final TeamRegion region;
    private final TeamRegion netherRegion;
    private final String prefix;
    private final TopMoneyService topMoneyService;
    @Getter @Setter
    private int additionalMembers = 0;

    public Team(String id, TeamGuiElement guiElement, TeamRegion region,
                TeamRegion netherRegion,
                Set<TeamMember> members, String prefix) {

        this.id = id;
        this.guiElement = guiElement;
        this.region = region;
        this.netherRegion = netherRegion;
        this.members = members;
        this.prefix = prefix;

        final var wgRegion = region.getRegion();
        this.members.forEach(member ->
                wgRegion.getMembers().addPlayer(member.getPlayerName()));

        this.topMoneyService = new TopMoneyService(this);
    }

    public void clearMembers() {
        this.members.forEach(m -> {
            if (m.getBukkitInstance() != null) {
                TeamWars.getInstance().getBorderManager().resetBorder(m.getBukkitInstance());
            }
        });
        this.members.clear();
    }

    public double getFullBalance() {
        double balance = 0;
        for (TeamMember member: members) {
            balance += member.getBalance();
        }
        return balance;
    }

    public void syncMembersBorder() {
        for (TeamMember member: members) {
            if (member.getBukkitInstance() != null)
                syncWorldBorder(member.getBukkitInstance());
        }
    }

    public void syncWorldBorder(Player player) {
        final World world = player.getWorld();
        final TeamRegion r = switch (world.getEnvironment()) {
            case NORMAL -> region;
            case NETHER -> netherRegion;
            default -> null;
        };

        if (r == null) return;

        TeamWars.getInstance().getBorderManager().setBorder(player,
                r.calculateCenter(), r.calculateSideLength());
    }

    public void clearInventories() {
        members.forEach(member -> {
            Player player = member.getBukkitInstance();
            if (player != null)
                player.getInventory().clear();
        });
    }

    public void teleport(Location location) {
        members.forEach(member -> {
            Player player = member.getBukkitInstance();
            if (player != null)
                player.teleport(location);
        });
    }

    public JsonObject getJson() {
        JsonObject jo = new JsonObject();
        jo.add("id", new JsonPrimitive(id));
        JsonArray members = new JsonArray();
        for (TeamMember member: this.members) {
            members.add(member.getJson());
        }
        jo.add("members", members);
        return jo;
    }

    public boolean isMember(String playerName) {
        for (TeamMember member: members) {
            if (member.getPlayerName().equalsIgnoreCase(playerName))
                return true;
        }
        return false;
    }

    public void teleportMember(Player player, Location to) {
        final Location old = player.getLocation().clone();
        player.teleport(to);
        if (old.getWorld() != to.getWorld()) {
            syncWorldBorder(player);
        }
    }

    public void sendTitle(Title title) {
        for (TeamMember member: members) {
            final Player player = member.getBukkitInstance();
            if (player == null) continue;
            player.showTitle(title);
        }
    }
    public void sendMessage(String message) {
        members.forEach(member -> {
            Player player = member.getBukkitInstance();
            if (player != null)
                player.sendMessage(prefix+message);
        });
    }

    public @Nullable TeamMember getMember(String playerName) {
        for (TeamMember member: members) {
            if (member.getPlayerName().equalsIgnoreCase(playerName))
                return member;
        }
        return null;
    }

    public int getActiveMembersCount() {
        int count = 0;
        for (TeamMember member: members) {
            if (member.isActive())
                count++;
        }
        return count;
    }

    public void addMember(TeamMember member) {
        this.members.add(member);
        region.getRegion().getMembers().addPlayer(member.getPlayerName());
        netherRegion.getRegion().getMembers().addPlayer(member.getPlayerName());
    }

    public TeamMember addMember(@NotNull Player player) {
        final TeamMember member = new TeamMember(player.getName(), true, 0,0, 0, player);
        addMember(member);

        return member;
    }


    public void kickMember(TeamMember member) {
        members.remove(member);
        region.getRegion().getMembers().removePlayer(member.getPlayerName());
        netherRegion.getRegion().getMembers().removePlayer(member.getPlayerName());
    }

    /**
     * Клонирует команду со всеми настройками,
     * кроме участников
     * @return Team
     */
    public Team clone() {
        return new Team(id, guiElement, region, netherRegion, Collections.emptySet(), prefix);
    }
}
