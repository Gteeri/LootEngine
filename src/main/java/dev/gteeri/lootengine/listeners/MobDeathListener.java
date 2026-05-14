package dev.gteeri.lootengine.listeners;

import dev.gteeri.lootengine.LootEngine;
import dev.gteeri.lootengine.loot.LootEntry;
import dev.gteeri.lootengine.loot.Rarity;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Listens for mob deaths and handles custom loot drops.
 */
public class MobDeathListener implements Listener {

    private final LootEngine plugin;

    public MobDeathListener(LootEngine plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) return;

        List<LootEntry> entries = plugin.getLootManager().getLootFor(entity.getType());
        if (entries.isEmpty()) return;

        Location dropLocation = entity.getLocation();

        for (LootEntry entry : entries) {
            if (!entry.rollChance()) continue;

            ItemStack item = entry.createItem();

            // Drop the item at mob's location
            entity.getWorld().dropItemNaturally(dropLocation, item);

            // Play effects for rare+ drops
            if (entry.getRarity().ordinal() >= Rarity.RARE.ordinal()) {
                playDropEffects(dropLocation, entry, killer);
            }

            // Broadcast legendary drops
            if (entry.getRarity() == Rarity.LEGENDARY) {
                broadcastLegendaryDrop(killer, item, entity);
            }
        }
    }

    private void playDropEffects(Location location, LootEntry entry, Player player) {
        if (entry.hasParticles()) {
            location.getWorld().spawnParticle(
                    Particle.HAPPY_VILLAGER,
                    location.add(0, 1, 0),
                    15, 0.5, 0.5, 0.5, 0.1
            );
        }

        if (entry.getSound() != null) {
            try {
                Sound sound = Sound.valueOf(entry.getSound());
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException ignored) {
                // Invalid sound name in config, skip silently
            }
        }
    }

    private void broadcastLegendaryDrop(Player player, ItemStack item, LivingEntity mob) {
        String message = ChatColor.YELLOW + "" + ChatColor.BOLD + "[LOOT] "
                + ChatColor.WHITE + player.getName()
                + ChatColor.GRAY + " got "
                + ChatColor.GOLD + getItemName(item)
                + ChatColor.GRAY + " from "
                + ChatColor.WHITE + formatMobName(mob.getType().name()) + "!";

        Bukkit.broadcastMessage(message);
    }

    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return formatMobName(item.getType().name());
    }

    private String formatMobName(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            result.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(" ");
        }
        return result.toString().trim();
    }
}
