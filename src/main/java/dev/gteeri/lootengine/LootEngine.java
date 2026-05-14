package dev.gteeri.lootengine;

import dev.gteeri.lootengine.commands.LootCommand;
import dev.gteeri.lootengine.listeners.MobDeathListener;
import dev.gteeri.lootengine.loot.LootManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * LootEngine - Advanced custom loot drop system for Paper 1.21+
 * Provides configurable mob drops with rarities, custom items,
 * particle effects and GUI preview.
 *
 * @author Gteeri
 */
public final class LootEngine extends JavaPlugin {

    private static LootEngine instance;
    private LootManager lootManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("loot.yml", false);

        this.lootManager = new LootManager(this);
        this.lootManager.loadLootTables();

        getServer().getPluginManager().registerEvents(new MobDeathListener(this), this);
        getCommand("loot").setExecutor(new LootCommand(this));

        getLogger().info("LootEngine v" + getDescription().getVersion() + " enabled!");
        getLogger().info("Loaded " + lootManager.getTableCount() + " loot tables.");
    }

    @Override
    public void onDisable() {
        getLogger().info("LootEngine disabled.");
    }

    public static LootEngine getInstance() {
        return instance;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public void reload() {
        reloadConfig();
        lootManager.loadLootTables();
        getLogger().info("LootEngine configuration reloaded.");
    }
}
