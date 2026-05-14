package dev.gteeri.lootengine.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Prevents players from taking items out of the loot preview GUI.
 */
public class GUIClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Check if it's our GUI by looking at the title format
        if (title.startsWith(ChatColor.DARK_GRAY + "Loot") ||
                title.contains("Loot Table") ||
                title.contains("Таблица лута") ||
                title.contains("Tabla de Loot") ||
                title.contains("Loot-Tabelle")) {
            event.setCancelled(true);
        }
    }
}
