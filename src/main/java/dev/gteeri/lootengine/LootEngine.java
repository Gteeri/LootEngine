package dev.gteeri.lootengine;

import dev.gteeri.lootengine.commands.LootCommand;
import dev.gteeri.lootengine.lang.MessageManager;
import dev.gteeri.lootengine.listeners.GUIClickListener;
import dev.gteeri.lootengine.listeners.MobDeathListener;
import dev.gteeri.lootengine.loot.LootManager;
import dev.gteeri.lootengine.stats.StatsManager;
import dev.gteeri.lootengine.util.CooldownManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LootEngine extends JavaPlugin {

    private static LootEngine instance;
    private LootManager lootManager;
    private MessageManager messageManager;
    private StatsManager statsManager;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("loot.yml", false);

        this.messageManager = new MessageManager(this);
        this.lootManager = new LootManager(this);
        this.lootManager.loadLootTables();
        this.statsManager = new StatsManager(this);

        int cooldownSeconds = getConfig().getInt("drops.cooldown-seconds", 0);
        this.cooldownManager = new CooldownManager(cooldownSeconds);

        getServer().getPluginManager().registerEvents(new MobDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIClickListener(), this);

        LootCommand lootCommand = new LootCommand(this);
        getCommand("loot").setExecutor(lootCommand);
        getCommand("loot").setTabCompleter(lootCommand);

        getLogger().info("LootEngine v" + getDescription().getVersion() + " enabled");
        getLogger().info("Language: " + messageManager.getCurrentLanguage());
        getLogger().info("Loot tables loaded: " + lootManager.getTableCount());
    }

    @Override
    public void onDisable() {
        if (statsManager != null) {
            statsManager.saveStats();
        }
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

    public void reload() {
        reloadConfig();
        messageManager.loadLanguage();
        lootManager.loadLootTables();

        int cooldownSeconds = getConfig().getInt("drops.cooldown-seconds", 0);
        cooldownManager = new CooldownManager(cooldownSeconds);
    }
}
