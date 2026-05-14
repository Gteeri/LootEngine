package dev.gteeri.lootengine.stats;

import dev.gteeri.lootengine.LootEngine;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player statistics persistence.
 * Currently supports YAML storage with future MySQL support planned.
 */
public class StatsManager {

    private final LootEngine plugin;
    private final Map<UUID, PlayerStats> statsCache = new HashMap<>();
    private final File statsFile;

    public StatsManager(LootEngine plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        loadStats();
    }

    /**
     * Gets or creates stats for a player.
     *
     * @param playerId the player's UUID
     * @return the player's stats object
     */
    public PlayerStats getStats(UUID playerId) {
        return statsCache.computeIfAbsent(playerId, PlayerStats::new);
    }

    /**
     * Records a kill for a player.
     *
     * @param playerId the player's UUID
     * @param mobType  the type of mob killed
     */
    public void recordKill(UUID playerId, String mobType) {
        getStats(playerId).addKill(mobType);
    }

    /**
     * Records a drop for a player.
     *
     * @param playerId    the player's UUID
     * @param isLegendary whether the drop was legendary
     */
    public void recordDrop(UUID playerId, boolean isLegendary) {
        getStats(playerId).addDrop(isLegendary);
    }

    /**
     * Saves all stats to disk.
     */
    public void saveStats() {
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, PlayerStats> entry : statsCache.entrySet()) {
            String path = entry.getKey().toString();
            PlayerStats stats = entry.getValue();

            config.set(path + ".kills", stats.getTotalKills());
            config.set(path + ".drops", stats.getTotalDrops());
            config.set(path + ".legendaries", stats.getLegendaryDrops());

            for (Map.Entry<String, Integer> mobEntry : stats.getKillsByMob().entrySet()) {
                config.set(path + ".mobs." + mobEntry.getKey(), mobEntry.getValue());
            }
        }

        try {
            config.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save stats: " + e.getMessage());
        }
    }

    /**
     * Loads stats from disk.
     */
    private void loadStats() {
        if (!statsFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(statsFile);

        for (String uuidStr : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                PlayerStats stats = new PlayerStats(uuid);

                int kills = config.getInt(uuidStr + ".kills", 0);
                int drops = config.getInt(uuidStr + ".drops", 0);
                int legendaries = config.getInt(uuidStr + ".legendaries", 0);

                // Restore counts via reflection-free approach
                for (int i = 0; i < kills; i++) stats.addKill("unknown");
                for (int i = 0; i < drops - legendaries; i++) stats.addDrop(false);
                for (int i = 0; i < legendaries; i++) stats.addDrop(true);

                statsCache.put(uuid, stats);
            } catch (IllegalArgumentException ignored) {
                // Skip invalid UUIDs
            }
        }

        plugin.getLogger().info("Loaded stats for " + statsCache.size() + " players.");
    }
}
