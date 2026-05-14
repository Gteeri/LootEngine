package dev.gteeri.lootengine.loot;

import dev.gteeri.lootengine.LootEngine;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.*;

/**
 * Manages all loot tables loaded from configuration.
 * Handles loading, reloading and querying loot entries.
 */
public class LootManager {

    private final LootEngine plugin;
    private final Map<EntityType, List<LootEntry>> lootTables = new HashMap<>();

    public LootManager(LootEngine plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads all loot tables from the loot.yml configuration file.
     * Clears existing tables before loading.
     */
    public void loadLootTables() {
        lootTables.clear();

        File lootFile = new File(plugin.getDataFolder(), "loot.yml");
        if (!lootFile.exists()) {
            plugin.getLogger().warning("loot.yml not found!");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(lootFile);
        ConfigurationSection tables = config.getConfigurationSection("tables");

        if (tables == null) {
            plugin.getLogger().warning("No loot tables found in loot.yml");
            return;
        }

        for (String mobName : tables.getKeys(false)) {
            EntityType entityType = parseEntityType(mobName);
            if (entityType == null) {
                plugin.getLogger().warning("Unknown entity type: " + mobName);
                continue;
            }

            List<LootEntry> entries = new ArrayList<>();
            List<?> itemList = tables.getList(mobName);

            if (itemList == null) continue;

            ConfigurationSection mobSection = tables.getConfigurationSection(mobName);
            if (mobSection != null) {
                // Handle as list of maps
                for (String key : mobSection.getKeys(false)) {
                    // Not used in this format
                }
            }

            // Parse each entry from the list
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

        plugin.getLogger().info("Loaded " + lootTables.size() + " loot tables.");
    }

    /**
     * Gets the loot entries for a specific entity type.
     *
     * @param type the entity type to get loot for
     * @return list of loot entries, or empty list if none configured
     */
    public List<LootEntry> getLootFor(EntityType type) {
        return lootTables.getOrDefault(type, Collections.emptyList());
    }

    /**
     * Gets all configured entity types that have loot tables.
     *
     * @return set of entity types with configured loot
     */
    public Set<EntityType> getConfiguredMobs() {
        return Collections.unmodifiableSet(lootTables.keySet());
    }

    /**
     * Gets the total number of loaded loot tables.
     *
     * @return number of loot tables
     */
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
