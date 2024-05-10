package eventserver.teamwars.game.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public class TeamRegion {
    private final World world;
    private Location spawn;
    private ProtectedCuboidRegion region;

    private Point min, max;

    public void setPoints(Point min, Point max) {
        this.min = min;
        this.max = max;
        spawn = calculateCenter().toHighestLocation().add(0, 1, 0);

        if (this.region != null) {
            updateRegion(min, max);
        }
    }

    public Location calculateCenter() {
        double centerX = (min.getX() + max.getX()) / 2.0;
        double centerY = (min.getY() + max.getY()) / 2.0;
        double centerZ = (min.getZ() + max.getZ()) / 2.0;

        return new Location(world, centerX, centerY, centerZ);
    }

    public double calculateSideLength() {
        double deltaX = Math.abs(max.getX() - min.getX());
        double deltaZ = Math.abs(max.getZ() - min.getZ());

        return Math.max(deltaX, deltaZ);
    }

    public void outlineRegion(Material material) {
        int minX = (int) Math.min(min.x, max.x);
        int minY = (int) Math.min(min.y, max.y);
        int minZ = (int) Math.min(min.z, max.z);
        int maxX = (int) Math.max(min.x, max.x);
        int maxY = (int) Math.max(min.y, max.y);
        int maxZ = (int) Math.max(min.z, max.z);

        // Строим бедрок только по краям куба
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, minY, z).setType(Material.BEDROCK); // Нижний слой
                world.getBlockAt(x, maxY, z).setType(Material.BEDROCK); // Верхний слой
            }
        }

        for (int y = minY + 1; y < maxY; y++) {
            world.getBlockAt(minX, y, minZ).setType(Material.BEDROCK); // Левая грань
            world.getBlockAt(minX, y, maxZ).setType(Material.BEDROCK); // Левая грань
            world.getBlockAt(maxX, y, minZ).setType(Material.BEDROCK); // Правая грань
            world.getBlockAt(maxX, y, maxZ).setType(Material.BEDROCK); // Правая грань
        }
    }

    private void updateRegion(Point min, Point max) {
        var newRegion = new ProtectedCuboidRegion(region.getId(), BlockVector3.at(min.x, min.y, min.z), BlockVector3.at(max.x, max.y, max.z));
        newRegion.setMembers(region.getMembers());
        newRegion.setOwners(region.getOwners());
        newRegion.setFlags(region.getFlags());
        final var container = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        assert container != null;
        container.removeRegion(region.getId());
        container.addRegion(newRegion);
        this.region = newRegion;
    }

    public TeamRegion(World world, ConfigurationSection section) {
        this.world = world;

        this.min = new Point(section.getConfigurationSection("min"));
        this.max = new Point(section.getConfigurationSection("max"));

        spawn = calculateCenter().toHighestLocation().add(0, 1, 0);
    }

    public void createWgRegion(String teamId) {
        this.region = new ProtectedCuboidRegion("TW-team-"+ teamId,
                BlockVector3.at(this.min.x, this.min.y, this.min.z),
                BlockVector3.at(this.max.x, this.max.y, this.max.z));
    }

    @Getter @ToString
    public static class Point {
        private final ConfigurationSection section;
        private double x, y, z;

        public Point(ConfigurationSection section) {
            this.section = section;
            loadCoords(section);
        }

        public void setX(double x) {
            this.x = x;
            section.set("x", x);
        }

        public void setY(double y) {
            this.y = y;
            section.set("y", y);
        }

        public void setZ(double z) {
            this.z = z;
            section.set("z", z);
        }

        private void loadCoords(ConfigurationSection section) {
            this.x = section.getDouble("x");
            this.y = section.getDouble("y");
            this.z = section.getDouble("z");
        }
    }
}
