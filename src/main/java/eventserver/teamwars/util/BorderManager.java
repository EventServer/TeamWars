package eventserver.teamwars.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BorderManager implements Listener {
    private final Map<Player, BorderState> cache = new ConcurrentHashMap<>();

    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public BorderManager(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public BorderState getState(Player player) {
        if (cache.containsKey(player)) {
                return cache.get(player);

        }
        final WorldBorder border = player.getWorld().getWorldBorder();
        return new BorderState(border.getCenter(), border.getSize());
    }


    public void setBorder(Player player, Location center, double size) {
        PacketContainer worldBorderCenter = protocolManager.createPacket(PacketType.Play.Server.WORLD_BORDER);
        worldBorderCenter.getWorldBorderActions().write(0, EnumWrappers.WorldBorderAction.SET_CENTER);
        worldBorderCenter.getDoubles().write(0, center.getX()); // Center X
        worldBorderCenter.getDoubles().write(1, center.getZ()); // Center Z

        PacketContainer worldBorderSize = protocolManager.createPacket(PacketType.Play.Server.WORLD_BORDER);
        worldBorderSize.getWorldBorderActions().write(0, EnumWrappers.WorldBorderAction.SET_SIZE);
        worldBorderSize.getDoubles().write(2, size);
        worldBorderSize.getDoubles().write(3, size);
        protocolManager.sendServerPacket(player, worldBorderCenter);
        protocolManager.sendServerPacket(player, worldBorderSize);
        cache.put(player, new BorderState(center, size));
    }

    public void resetBorder(Player player) {
        final World world = player.getWorld();
        final WorldBorder border = world.getWorldBorder();
        setBorder(player, border.getCenter(), border.getSize());
        cache.remove(player);
    }

    public record BorderState(Location center, double size) {
    }

    @EventHandler
    private void on(PlayerQuitEvent event) {
        cache.remove(event.getPlayer());
    }
}
