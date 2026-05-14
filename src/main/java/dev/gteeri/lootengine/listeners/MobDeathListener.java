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

        if (!isWorldAllowed(killer.getWorld().getName())) return;

        String mobType = entity.getType().name();
        if (plugin.getCooldownManager().isOnCooldown(killer.getUniqueId(), mobType)) return;

        List<LootEntry> entries = plugin.getLootManager().getLootFor(entity.getType());
        if (entries.isEmpty()) return;

        plugin.getStatsManager().recordKill(killer.getUniqueId(), mobType);

        double multiplier = getChanceMultiplier(killer);
        Location dropLocation = entity.getLocation();
        FileConfiguration config = plugin.getConfig();
        int maxDrops = config.getInt("drops.max-drops-per-kill", 3);
        boolean dropToInventory = config.getBoolean("drops.drop-to-inventory", false);

        int dropCount = 0;

        for (LootEntry entry : entries) {
            if (dropCount >= maxDrops) break;

            if (entry.getCondition() != null &&
                    !entry.getCondition().check(killer, killer.getWorld())) continue;

            double effectiveChance = entry.getChance() * multiplier;
            if (Math.random() * 100.0 >= effectiveChance) continue;

            ItemStack item = entry.createItem();

            if (dropToInventory) {
                Map<Integer, ItemStack> overflow = killer.getInventory().addItem(item);
                overflow.values().forEach(i -> killer.getWorld().dropItemNaturally(dropLocation, i));
            } else {
                entity.getWorld().dropItemNaturally(dropLocation, item);
            }

            dropCount++;
            plugin.getStatsManager().recordDrop(killer.getUniqueId(), entry.getRarity() == Rarity.LEGENDARY);

            if (entry.getRarity().ordinal() >= Rarity.RARE.ordinal()) {
                playEffects(dropLocation, entry, killer);
            }

            if (entry.getRarity() == Rarity.LEGENDARY) {
                broadcastDrop(killer, item, entity);
            }
        }

        if (dropCount > 0) {
            plugin.getCooldownManager().setCooldown(killer.getUniqueId(), mobType);
        }
    }

    private boolean isWorldAllowed(String worldName) {
        FileConfiguration config = plugin.getConfig();
        List<String> list = config.getStringList("worlds.blacklist");
        boolean whitelist = config.getBoolean("worlds.use-as-whitelist", false);
        return whitelist ? list.contains(worldName) : !list.contains(worldName);
    }

    private double getChanceMultiplier(Player player) {
        FileConfiguration config = plugin.getConfig();
        double base = config.getDouble("drops.chance-multiplier", 1.0);

        if (config.contains("permission-multipliers")) {
            for (String perm : config.getConfigurationSection("permission-multipliers").getKeys(false)) {
                if (player.hasPermission(perm)) {
                    base = Math.max(base, config.getDouble("permission-multipliers." + perm, 1.0));
                }
            }
        }

        return base;
    }

    private void playEffects(Location location, LootEntry entry, Player player) {
        if (entry.hasParticles()) {
            FileConfiguration config = plugin.getConfig();
            try {
                Particle particle = Particle.valueOf(config.getString("particles.type", "HAPPY_VILLAGER"));
                int count = config.getInt("particles.count", 20);
                double spread = config.getDouble("particles.spread", 0.5);
                location.getWorld().spawnParticle(particle, location.clone().add(0, 1, 0),
                        count, spread, spread, spread, 0.1);
            } catch (IllegalArgumentException ignored) {}
        }

        if (entry.getSound() != null) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(entry.getSound()), 1.0f, 1.2f);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private void broadcastDrop(Player player, ItemStack item, LivingEntity mob) {
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
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
