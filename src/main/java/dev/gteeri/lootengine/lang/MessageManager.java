package dev.gteeri.lootengine.lang;

import dev.gteeri.lootengine.LootEngine;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages multi-language message loading and retrieval.
 * Supports en, ru, es, de with fallback to English.
 */
public class MessageManager {

    private final LootEngine plugin;
    private YamlConfiguration messages;
    private String currentLanguage;

    private static final String[] SUPPORTED_LANGUAGES = {"en", "ru", "es", "de"};

    public MessageManager(LootEngine plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    /**
     * Loads the language file based on config.yml setting.
     * Falls back to English if the specified language is not found.
     */
    public void loadLanguage() {
        String lang = plugin.getConfig().getString("language", "en").toLowerCase();

        // Save all language files
        for (String supported : SUPPORTED_LANGUAGES) {
            String path = "lang/" + supported + ".yml";
            File file = new File(plugin.getDataFolder(), path);
            if (!file.exists()) {
                plugin.saveResource(path, false);
            }
        }

        // Load the selected language
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file '" + lang + ".yml' not found, falling back to English.");
            langFile = new File(plugin.getDataFolder(), "lang/en.yml");
        }

        this.messages = YamlConfiguration.loadConfiguration(langFile);
        this.currentLanguage = lang;

        // Load defaults from jar as fallback
        InputStream defaultStream = plugin.getResource("lang/en.yml");
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            this.messages.setDefaults(defaults);
        }

        plugin.getLogger().info("Loaded language: " + lang);
    }

    /**
     * Gets a message by its key path, with color codes translated.
     *
     * @param key the message key (e.g., "commands.reload-success")
     * @return the formatted message
     */
    public String get(String key) {
        String message = messages.getString(key);
        if (message == null) {
            return ChatColor.RED + "Missing message: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Gets a message with placeholder replacements.
     *
     * @param key          the message key
     * @param placeholders map of placeholder → value pairs
     * @return the formatted message with placeholders replaced
     */
    public String get(String key, Map<String, String> placeholders) {
        String message = get(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    /**
     * Gets a prefixed message (prefix + message).
     *
     * @param key the message key
     * @return the prefixed formatted message
     */
    public String getPrefixed(String key) {
        return get("prefix") + get(key);
    }

    /**
     * Gets a prefixed message with placeholders.
     *
     * @param key          the message key
     * @param placeholders placeholder map
     * @return the prefixed formatted message
     */
    public String getPrefixed(String key, Map<String, String> placeholders) {
        return get("prefix") + get(key, placeholders);
    }

    /**
     * Helper to create a placeholder map quickly.
     */
    public static Map<String, String> of(String... pairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.length - 1; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
