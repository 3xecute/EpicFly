package dev.execute.epicfly.managers;

import dev.execute.epicfly.EpicFly;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LanguageManager {

    private final EpicFly plugin;
    private FileConfiguration messages;

    public LanguageManager(EpicFly plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        String lang = plugin.getConfig().getString("language", "en");

        // Save default language files
        saveDefault("messages/en.yml");
        saveDefault("messages/tr.yml");
        saveDefault("messages/ru.yml");

        // Determine file
        String fileName = "messages/" + lang + ".yml";
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            plugin.getLogger().warning("Language file not found: " + fileName + ", falling back to en.yml");
            file = new File(plugin.getDataFolder(), "messages/en.yml");
        }

        messages = YamlConfiguration.loadConfiguration(file);

        // Fill missing keys from en.yml as fallback
        InputStream defStream = plugin.getResource("messages/en.yml");
        if (defStream != null) {
            FileConfiguration def = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8));
            messages.setDefaults(def);
        }

        plugin.getLogger().info("Language loaded: " + lang);
    }

    public String get(String key) {
        String val = messages.getString(key, "&cMissing: " + key);
        return colorize(val);
    }

    public String get(String key, String... replacements) {
        String val = get(key);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            val = val.replace(replacements[i], replacements[i + 1]);
        }
        return val;
    }

    public String getPrefix() {
        return get("prefix");
    }

    public String withPrefix(String key, String... replacements) {
        return getPrefix() + get(key, replacements);
    }

    public static String colorize(String text) {
        if (text == null) return "";
        return text.replace("&", "§");
    }

    private void saveDefault(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(resourcePath, false);
        }
    }
}
