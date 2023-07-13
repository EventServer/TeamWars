package eventserver.teamwars.game;

import eventserver.teamwars.Config;
import eventserver.teamwars.TeamWars;
import eventserver.teamwars.event.MemberBuyReturnInventoryEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class InventoryReturnManager {
    private final Map<Player, Map<Integer, ItemStack>> inventories = new HashMap<>();

    private final Game game;

    public InventoryReturnManager(Game game) {
        this.game = game;
    }

    public void onDeath(PlayerDeathEvent event) {
        final Player player = event.getPlayer();
        Team team = game.getTeamManager().getPlayerTeam(player);
        if (team == null) return;

        event.getDrops().clear();
        event.setCancelled(true);

        final Map<Integer, ItemStack> items = new HashMap<>();
        final PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContents().length; i++) {
            final ItemStack stack = inventory.getItem(i);
            if (stack != null) {
                items.put(i, stack);
            }
        }
        inventory.clear();
        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        player.setHealth(player.getMaxHealth());
        player.setExp(0);
        player.setLevel(0);
        Bukkit.getScheduler().runTaskLater(TeamWars.getInstance(), () -> {
            player.teleport(team.getSpawn());
        }, 2);
        inventories.put(player, items);

        for (String str: Config.MESSAGES.KEEP_INVENTORY_NOTIFY) {
            player.sendMessage(str);
        }
    }

    public void clear() {
        this.inventories.clear();
    }

    public boolean isContains(Player player) {
        return inventories.containsKey(player);
    }

    public void returnInventory(Player player) {
        final Map<Integer, ItemStack> items = inventories.get(player);
        if (items == null) return;
        final PlayerInventory inventory = player.getInventory();
        items.forEach((slot, item) -> {
            final ItemStack invStack = inventory.getItem(slot);
            if (invStack == null || invStack.getType() == Material.AIR) {
                inventory.setItem(slot, item);
            } else {
                inventory.addItem(item)
                        .forEach((s, i) -> player.getWorld().dropItem(player.getLocation(), i));
            }
        });
        inventories.remove(player);
    }
}
