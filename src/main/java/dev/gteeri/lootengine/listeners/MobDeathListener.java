package dev.gteeri.lootengine.listeners;

import dev.gteeri.lootengine.LootEngine;
import dev.gteeri.lootengine.lang.MessageManager;
import dev.gteeri.lootengine.loot.LootEntry;
import dev.gteeri.lootengine.loot.Rarity;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Listens for mob deaths and handles custom loot drops.
 * Supports world blacklists, permission multipliers, cooldowns and stats tracking.
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

        // Check world blacklist
        if (!isWorldAllowed(killer.getWorld().getName())) return;

        // Check cooldown
        String mobType = entity.getType().name();
        if (plugin.getCooldownManager().isOnCooldown(killer.getUniqueId(), mobType)) return;

        List<LootEntry> entries = plugin.getLootManager().getLootFor(entity.getType());
        if (entries.isEmpty()) return;

        // Track kill
        plugin.getStatsManager().recordKill(killer.getUniqueId(), mobType);

        // Get chance multiplier
        double multiplier = getChanceMultiplier(killer);

        Location dropLocation = entity.getLocation();
        FileConfiguration config = plugin.getConfig();
        int maxDrops = config.getInt("drops.max-drops-per-kill", 3);
        boolean dropToInventory = config.getBoolean("drops.drop-to-inventory", false);

        int dropCount = 0;

        for (LootEntry entry : entries) {
            if (dropCount >= maxDrops) break;

            // Apply multiplier to chance
            double effectiveChance = entry.getChance() * multiplier;
            if (Math.random() * 100.0 >= effectiveChance) continue;

            ItemStack item = entry.createItem();

            // Drop or give to inventory
            if (dropToInventory) {
                Map<Integer, ItemStack> overflow = killer.getInventory().addItem(item);
                overflow.values().forEach(i -> killer.getWorld().dropItemNaturally(dropLocation, i));
            } else {
                entity.getWorld().dropItemNaturally(dropLocation, item);
            }

            dropCount++;

            // Track drop
            plugin.getStatsManager().recordDrop(killer.getUniqueId(),
                    entry.getRarity() == Rarity.LEGENDARY);

            // Play effects for rare+ drops
            if (entry.getRarity().ordinal() >= Rarity.RARE.ordinal()) {
                playDropEffects(dropLocation, entry, killer);
            }

            // Broadcast legendary drops
            if (entry.getRarity() == Rarity.LEGENDARY) {
                broadcastLegendaryDrop(killer, item, entity);
            }
        }

        // Set cooldown if any drops occurred
        if (dropCount > 0) {
            plugin.getCooldownManager().setCooldown(killer.getUniqueId(), mobType);
        }
    }

    private boolean isWorldAllowed(String worldName) {
        FileConfiguration config = plugin.getConfig();
        List<String> list = config.getStringList("worlds.blacklist");
        boolean useAsWhitelist = config.getBoolean("worlds.use-as-whitelist", false);

        if (useAsWhitelist) {
            return list.contains(worldName);
        } else {
            return !list.contains(worldName);
        }
    }

    private double getChanceMultiplier(Player player) {
        FileConfiguration config = plugin.getConfig();
        double baseMultiplier = config.getDouble("drops.chance-multiplier", 1.0);

        // Check permission-based multipliers
        if (config.contains("permission-multipliers")) {
            for (String perm : config.getConfigurationSection("permission-multipliers").getKeys(false)) {
                if (player.hasPermission(perm)) {
                    double permMultiplier = config.getDouble("permission-multipliers." + perm, 1.0);
                    baseMultiplier = Math.max(baseMultiplier, permMultiplier);
                }
            }
        }

        return baseMultiplier;
    }

    private void playDropEffects(Location location, LootEntry entry, Player player) {
        if (entry.hasParticles()) {
            FileConfiguration config = plugin.getConfig();
            String particleType = config.getString("particles.type", "HAPPY_VILLAGER");
            int count = config.getInt("particles.count", 20);
            double spread = config.getDouble("particles.spread", 0.5);

            try {
                Particle particle = Particle.valueOf(particleType);
                location.getWorld().spawnParticle(particle,
                        location.clone().add(0, 1, 0),
                        count, spread, spread, spread, 0.1);
            } catch (IllegalArgumentException ignored) {}
        }

        if (entry.getSound() != null) {
            try {
                Sound sound = Sound.valueOf(entry.getSound());
                player.playSound(player.getLocation(), sound, 1.0f, 1.2f);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private void broadcastLegendaryDrop(Player player, ItemStack item, LivingEntity mob) {
        MessageManager msg = plugin.getMessageManager();
        String message = msg.get("drops.broadcast", MessageManager.of(
                "player", player.getName(),
                "item", getItemName(item),
                "mob", formatName(mob.getType().name())
        ));

        Bukkit.broadcastMessage(message);
    }

    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return formatName(item.getType().name());
    }

    private String formatName(String name) {
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
