package eventserver.teamwars.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import eventserver.teamwars.gui.TeamGuiElement;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
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
    private final ProtectedCuboidRegion region;
    private final ProtectedCuboidRegion netherRegion;
    private final Location spawn;
    private final Location netherSpawn;
    private final String prefix;
    private final TopMoneyService topMoneyService;
    @Getter @Setter
    private int additionalMembers = 0;

    public Team(String id, TeamGuiElement guiElement, ProtectedCuboidRegion region, ProtectedCuboidRegion netherRegion, Location spawn, Location netherSpawn, Set<TeamMember> members, String prefix) {
        this.id = id;
        this.guiElement = guiElement;
        this.region = region;
        this.netherRegion = netherRegion;
        this.spawn = spawn;
        this.netherSpawn = netherSpawn;
        this.members = members;
        this.prefix = prefix;

        this.members.forEach(member ->
                region.getMembers().addPlayer(member.getPlayerName()));

        this.topMoneyService = new TopMoneyService(this);
    }

    public double getFullBalance() {
        double balance = 0;
        for (TeamMember member: members) {
            balance += member.getBalance();
        }
        return balance;
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
        region.getMembers().addPlayer(member.getPlayerName());
        netherRegion.getMembers().addPlayer(member.getPlayerName());
    }

    public TeamMember addMember(@NotNull Player player) {
        final TeamMember member = new TeamMember(player.getName(), true, 0, player);
        addMember(member);

        return member;
    }


    public void kickMember(TeamMember member) {
        members.remove(member);
        region.getMembers().removePlayer(member.getPlayerName());
        netherRegion.getMembers().removePlayer(member.getPlayerName());
    }

    /**
     * Клонирует команду со всеми настройками,
     * кроме участников
     * @return Team
     */
    public Team clone() {
        return new Team(id, guiElement, region, netherRegion, spawn, netherSpawn, Collections.emptySet(), prefix);
    }
}
