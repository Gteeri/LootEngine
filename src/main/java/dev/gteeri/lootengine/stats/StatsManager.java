package dev.gteeri.lootengine.stats;

import dev.gteeri.lootengine.LootEngine;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final LootEngine plugin;
    private final Map<UUID, PlayerStats> cache = new HashMap<>();
    private final File statsFile;

    public StatsManager(LootEngine plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        load();
    }

    public PlayerStats getStats(UUID playerId) {
        return cache.computeIfAbsent(playerId, PlayerStats::new);
    }

    public void recordKill(UUID playerId, String mobType) {
        getStats(playerId).addKill(mobType);
    }

    public void recordDrop(UUID playerId, boolean legendary) {
        getStats(playerId).addDrop(legendary);
    }

    public void saveStats() {
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, PlayerStats> entry : cache.entrySet()) {
            String path = entry.getKey().toString();
            PlayerStats stats = entry.getValue();

            config.set(path + ".kills", stats.getTotalKills());
            config.set(path + ".drops", stats.getTotalDrops());
            config.set(path + ".legendaries", stats.getLegendaryDrops());

            for (Map.Entry<String, Integer> mob : stats.getKillsByMob().entrySet()) {
                config.set(path + ".mobs." + mob.getKey(), mob.getValue());
            }
        }

        try {
            config.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save stats: " + e.getMessage());
        }
    }

    private void load() {
        if (!statsFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(statsFile);

        for (String uuidStr : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                PlayerStats stats = new PlayerStats(uuid);

                int kills = config.getInt(uuidStr + ".kills", 0);
                int drops = config.getInt(uuidStr + ".drops", 0);
                int legendaries = config.getInt(uuidStr + ".legendaries", 0);

                for (int i = 0; i < kills; i++) stats.addKill("unknown");
                for (int i = 0; i < drops - legendaries; i++) stats.addDrop(false);
                for (int i = 0; i < legendaries; i++) stats.addDrop(true);

                cache.put(uuid, stats);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
