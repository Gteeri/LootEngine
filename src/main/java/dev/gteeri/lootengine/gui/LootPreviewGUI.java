package dev.gteeri.lootengine.gui;

import dev.gteeri.lootengine.lang.MessageManager;
import dev.gteeri.lootengine.loot.LootEntry;
import dev.gteeri.lootengine.loot.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for previewing loot tables of specific mobs.
 * Shows all possible drops with their chances and rarities.
 */
public class LootPreviewGUI {

    private static final int GUI_SIZE = 54;

    /**
     * Opens the loot preview GUI for a player.
     *
     * @param player  the player to show the GUI to
     * @param mobType the mob type being previewed
     * @param entries the loot entries to display
     * @param msg     the message manager for localized strings
     */
    public static void open(Player player, EntityType mobType, List<LootEntry> entries, MessageManager msg) {
        String title = msg.get("gui.preview-title",
                MessageManager.of("mob", formatName(mobType.name())));
        Inventory gui = Bukkit.createInventory(null, GUI_SIZE, title);

        // Fill border with glass panes
        ItemStack border = createBorderItem();
        for (int i = 0; i < GUI_SIZE; i++) {
            if (isBorderSlot(i)) {
                gui.setItem(i, border);
            }
        }

        // Place loot entries
        int slot = 10;
        for (LootEntry entry : entries) {
            if (slot >= 44) break;
            if (isBorderSlot(slot)) {
                slot++;
                continue;
            }

            gui.setItem(slot, createPreviewItem(entry, msg));
            slot++;
        }

        player.openInventory(gui);
    }

    private static ItemStack createPreviewItem(LootEntry entry, MessageManager msg) {
        ItemStack item = new ItemStack(entry.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            Rarity rarity = entry.getRarity();
            String name = entry.getDisplayName() != null
                    ? ChatColor.translateAlternateColorCodes('&', entry.getDisplayName())
                    : rarity.getColor() + formatName(entry.getMaterial().name());

            meta.setDisplayName(name);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(msg.get("gui.rarity-label",
                    MessageManager.of("rarity", rarity.getDisplayName())));
            lore.add(msg.get("gui.chance-label",
                    MessageManager.of("chance", String.valueOf(entry.getChance()))));
            lore.add("");
            if (entry.hasParticles()) {
                lore.add(msg.get("gui.effects-label"));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private static boolean isBorderSlot(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        return row == 0 || row == 5 || col == 0 || col == 8;
    }

    private static String formatName(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }
}
