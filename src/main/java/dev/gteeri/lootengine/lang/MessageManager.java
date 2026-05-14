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

public class MessageManager {

    private final LootEngine plugin;
    private YamlConfiguration messages;
    private String currentLanguage;

    private static final String[] LANGUAGES = {"en", "ru", "es", "de"};

    public MessageManager(LootEngine plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void loadLanguage() {
        String lang = plugin.getConfig().getString("language", "en").toLowerCase();

        for (String l : LANGUAGES) {
            File file = new File(plugin.getDataFolder(), "lang/" + l + ".yml");
            if (!file.exists()) plugin.saveResource("lang/" + l + ".yml", false);
        }

        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            langFile = new File(plugin.getDataFolder(), "lang/en.yml");
            lang = "en";
        }

        this.messages = YamlConfiguration.loadConfiguration(langFile);
        this.currentLanguage = lang;

        InputStream defaults = plugin.getResource("lang/en.yml");
        if (defaults != null) {
            messages.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaults, StandardCharsets.UTF_8)));
        }
    }

    public String get(String key) {
        String msg = messages.getString(key);
        if (msg == null) return ChatColor.RED + "Missing: " + key;
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String get(String key, Map<String, String> placeholders) {
        String msg = get(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return msg;
    }

    public String getPrefixed(String key) {
        return get("prefix") + get(key);
    }

    public String getPrefixed(String key, Map<String, String> placeholders) {
        return get("prefix") + get(key, placeholders);
    }

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
