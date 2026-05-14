package dev.gteeri.lootengine.loot;

import dev.gteeri.lootengine.LootEngine;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.*;

public class LootManager {

    private final LootEngine plugin;
    private final Map<EntityType, List<LootEntry>> lootTables = new HashMap<>();

    public LootManager(LootEngine plugin) {
        this.plugin = plugin;
    }

    public void loadLootTables() {
        lootTables.clear();

        File lootFile = new File(plugin.getDataFolder(), "loot.yml");
        if (!lootFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(lootFile);
        ConfigurationSection tables = config.getConfigurationSection("tables");
        if (tables == null) return;

        for (String mobName : tables.getKeys(false)) {
            EntityType entityType = parseEntityType(mobName);
            if (entityType == null) {
                plugin.getLogger().warning("Unknown entity type: " + mobName);
                continue;
            }

            List<LootEntry> entries = new ArrayList<>();
            List<Map<?, ?>> mapList = tables.getMapList(mobName);

            for (Map<?, ?> entryMap : mapList) {
                LootEntry entry = LootEntry.fromMap(entryMap);
                if (entry != null) {
                    entries.add(entry);
                }
            }

            if (!entries.isEmpty()) {
                lootTables.put(entityType, entries);
            }
        }
    }

    public List<LootEntry> getLootFor(EntityType type) {
        return lootTables.getOrDefault(type, Collections.emptyList());
    }

    public Set<EntityType> getConfiguredMobs() {
        return Collections.unmodifiableSet(lootTables.keySet());
    }

    public int getTableCount() {
        return lootTables.size();
    }

    private EntityType parseEntityType(String name) {
        try {
            return EntityType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
