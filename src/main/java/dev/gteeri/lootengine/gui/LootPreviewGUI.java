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

public class LootPreviewGUI {

    private static final int SIZE = 54;

    public static void open(Player player, EntityType mobType, List<LootEntry> entries, MessageManager msg) {
        String title = msg.get("gui.preview-title", MessageManager.of("mob", formatName(mobType.name())));
        Inventory gui = Bukkit.createInventory(null, SIZE, title);

        ItemStack border = createPane();
        for (int i = 0; i < SIZE; i++) {
            if (isBorder(i)) gui.setItem(i, border);
        }

        int slot = 10;
        for (LootEntry entry : entries) {
            if (slot >= 44) break;
            if (isBorder(slot)) { slot++; continue; }
            gui.setItem(slot, buildItem(entry, msg));
            slot++;
        }

        player.openInventory(gui);
    }

    private static ItemStack buildItem(LootEntry entry, MessageManager msg) {
        ItemStack item = new ItemStack(entry.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Rarity rarity = entry.getRarity();
        String name = entry.getDisplayName() != null
                ? ChatColor.translateAlternateColorCodes('&', entry.getDisplayName())
                : rarity.getColor() + formatName(entry.getMaterial().name());
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(msg.get("gui.rarity-label", MessageManager.of("rarity", rarity.getDisplayName())));
        lore.add(msg.get("gui.chance-label", MessageManager.of("chance", String.valueOf(entry.getChance()))));
        if (entry.hasParticles()) {
            lore.add("");
            lore.add(msg.get("gui.effects-label"));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createPane() {
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); pane.setItemMeta(meta); }
        return pane;
    }

    private static boolean isBorder(int slot) {
        int row = slot / 9, col = slot % 9;
        return row == 0 || row == 5 || col == 0 || col == 8;
    }

    private static String formatName(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
