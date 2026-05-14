package dev.gteeri.lootengine;

import dev.gteeri.lootengine.commands.LootCommand;
import dev.gteeri.lootengine.lang.MessageManager;
import dev.gteeri.lootengine.listeners.MobDeathListener;
import dev.gteeri.lootengine.loot.LootManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * LootEngine - Advanced custom loot drop system for Paper 1.21+
 * Provides configurable mob drops with rarities, custom items,
 * particle effects, multi-language support and GUI preview.
 *
 * @author Gteeri
 */
public final class LootEngine extends JavaPlugin {

    private static LootEngine instance;
    private LootManager lootManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default configs
        saveDefaultConfig();
        saveResource("loot.yml", false);

        // Initialize managers
        this.messageManager = new MessageManager(this);
        this.lootManager = new LootManager(this);
        this.lootManager.loadLootTables();

        // Register listeners and commands
        getServer().getPluginManager().registerEvents(new MobDeathListener(this), this);

        LootCommand lootCommand = new LootCommand(this);
        getCommand("loot").setExecutor(lootCommand);
        getCommand("loot").setTabCompleter(lootCommand);

        getLogger().info("LootEngine v" + getDescription().getVersion() + " enabled!");
        getLogger().info("Language: " + messageManager.getCurrentLanguage());
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

    public MessageManager getMessageManager() {
        return messageManager;
    }

    /**
     * Reloads all configuration files and language.
     */
    public void reload() {
        reloadConfig();
        messageManager.loadLanguage();
        lootManager.loadLootTables();
        getLogger().info("LootEngine configuration reloaded.");
    }
}
