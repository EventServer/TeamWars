package eventserver.teamwars.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

@Getter @AllArgsConstructor
public class TeamGuiElement {
    private final int slot;
    private final String display;
    private final Material material;
    private final List<String> lore;

    public ItemStack get() {
        final ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(lore!=null?lore: Collections.emptyList());
        stack.setItemMeta(meta);
        return stack;
    }
}
