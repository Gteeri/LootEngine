package dev.gteeri.lootengine;

import dev.gteeri.lootengine.commands.LootCommand;
import dev.gteeri.lootengine.lang.MessageManager;
import dev.gteeri.lootengine.listeners.GUIClickListener;
import dev.gteeri.lootengine.listeners.MobDeathListener;
import dev.gteeri.lootengine.loot.LootManager;
import dev.gteeri.lootengine.stats.StatsManager;
import dev.gteeri.lootengine.util.CooldownManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * LootEngine - Advanced custom loot drop system for Paper 1.21+
 *
 * Features:
 * - Configurable mob drops with rarity tiers
 * - Multi-language support (en, ru, es, de)
 * - Permission-based chance multipliers
 * - World blacklist/whitelist
 * - Cooldown system to prevent farming
 * - Player statistics tracking
 * - GUI loot table preview
 * - Particle effects and sound on drops
 *
 * @author Gteeri
 * @version 1.1.0
 */
public final class LootEngine extends JavaPlugin {

    private static LootEngine instance;
    private LootManager lootManager;
    private MessageManager messageManager;
    private StatsManager statsManager;
    private CooldownManager cooldownManager;

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
        this.statsManager = new StatsManager(this);

        int cooldownSeconds = getConfig().getInt("drops.cooldown-seconds", 0);
        this.cooldownManager = new CooldownManager(cooldownSeconds);

        // Register listeners
        getServer().getPluginManager().registerEvents(new MobDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIClickListener(), this);

        // Register commands
        LootCommand lootCommand = new LootCommand(this);
        getCommand("loot").setExecutor(lootCommand);
        getCommand("loot").setTabCompleter(lootCommand);

        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        getLogger().info("  LootEngine v" + getDescription().getVersion() + " enabled!");
        getLogger().info("  Language: " + messageManager.getCurrentLanguage());
        getLogger().info("  Loot tables: " + lootManager.getTableCount());
        getLogger().info("  Cooldown: " + cooldownSeconds + "s");
        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Override
    public void onDisable() {
        // Save stats on shutdown
        if (statsManager != null) {
            statsManager.saveStats();
        }
        getLogger().info("LootEngine disabled. Stats saved.");
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

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    /**
     * Reloads all configuration files, language and loot tables.
     */
    public void reload() {
        reloadConfig();
        messageManager.loadLanguage();
        lootManager.loadLootTables();

        int cooldownSeconds = getConfig().getInt("drops.cooldown-seconds", 0);
        cooldownManager = new CooldownManager(cooldownSeconds);

        getLogger().info("LootEngine configuration reloaded.");
    }
}
