package dev.execute.epicfly.managers;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.models.FlyPackage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final EpicFly plugin;
    private List<FlyPackage> packages;

    public ConfigManager(EpicFly plugin) {
        this.plugin = plugin;
        load();
    }

    public void reload() {
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        packages = new ArrayList<>();
        List<?> pkgList = plugin.getConfig().getList("packages");
        if (pkgList == null) return;

        for (Object obj : pkgList) {
            try {
                java.util.Map<String, Object> map;
                if (obj instanceof java.util.Map) {
                    map = (java.util.Map<String, Object>) obj;
                } else {
                    continue;
                }

                int id = toInt(map.getOrDefault("id", 0));
                String name = colorize((String) map.getOrDefault("name", "Fly Package"));

                List<String> lore = new ArrayList<>();
                Object loreObj = map.get("lore");
                if (loreObj instanceof List<?> loreList) {
                    for (Object line : loreList) {
                        lore.add(colorize(String.valueOf(line)));
                    }
                }

                Material material = Material.matchMaterial((String) map.getOrDefault("material", "FEATHER"));
                if (material == null) material = Material.FEATHER;
                long duration = toLong(map.getOrDefault("duration", 60));
                double price = toDouble(map.getOrDefault("price", 100));
                int slot = toInt(map.getOrDefault("slot", 10));
                packages.add(new FlyPackage(id, name, lore, material, duration, price, slot));
            } catch (Exception e) {
                plugin.getLogger().warning("Paket yüklenirken hata: " + e.getMessage());
            }
        }
    }

    private int toInt(Object o) {
        if (o instanceof Integer i) return i;
        if (o instanceof Number n) return n.intValue();
        return 0;
    }

    private long toLong(Object o) {
        if (o instanceof Long l) return l;
        if (o instanceof Number n) return n.longValue();
        return 0;
    }

    private double toDouble(Object o) {
        if (o instanceof Double d) return d;
        if (o instanceof Number n) return n.doubleValue();
        return 0;
    }

    public List<FlyPackage> getPackages() { return packages; }

    // Economy
    public boolean isEconomyEnabled() { return plugin.getConfig().getBoolean("economy.enabled", true); }
    public String getCurrencySymbol() { return plugin.getConfig().getString("economy.currency-symbol", "$"); }

    // Discount
    public boolean isDiscountEnabled() { return plugin.getConfig().getBoolean("discount.enabled", true); }
    public double getDiscountChance() { return plugin.getConfig().getDouble("discount.chance", 0.75); }
    public int getDiscountAmount() { return plugin.getConfig().getInt("discount.amount", 50); }

    // Fly
    public int getWarnBeforeEnd() { return plugin.getConfig().getInt("fly.warn-before-end", 60); }
    public boolean isCombatEnabled() { return plugin.getConfig().getBoolean("fly.combat.enabled", true); }
    public int getCombatDuration() { return plugin.getConfig().getInt("fly.combat.duration", 15); }
    public boolean isAfkEnabled() { return plugin.getConfig().getBoolean("fly.afk.enabled", true); }
    public int getAfkTimeout() { return plugin.getConfig().getInt("fly.afk.timeout", 30); }
    public boolean isSlowFallEnabled() { return plugin.getConfig().getBoolean("fly.slow-fall-on-expire", true); }
    public int getSlowFallDuration() { return plugin.getConfig().getInt("fly.slow-fall-duration", 5); }
    public List<String> getDisabledWorlds() { return plugin.getConfig().getStringList("fly.disabled-worlds"); }

    // Leaderboard
    public boolean isLeaderboardEnabled() { return plugin.getConfig().getBoolean("leaderboard.enabled", true); }
    public int getLeaderboardUpdateInterval() { return plugin.getConfig().getInt("leaderboard.update-interval", 300); }

    // Database
    public String getDatabaseType() { return plugin.getConfig().getString("database.type", "SQLITE").toUpperCase(); }
    public String getMysqlHost() { return plugin.getConfig().getString("database.mysql.host", "localhost"); }
    public int getMysqlPort() { return plugin.getConfig().getInt("database.mysql.port", 3306); }
    public String getMysqlDatabase() { return plugin.getConfig().getString("database.mysql.database", "epicfly"); }
    public String getMysqlUsername() { return plugin.getConfig().getString("database.mysql.username", "root"); }
    public String getMysqlPassword() { return plugin.getConfig().getString("database.mysql.password", ""); }
    public int getMysqlPoolSize() { return plugin.getConfig().getInt("database.mysql.pool-size", 10); }

    // Messages
    public String getMessage(String key) {
        String prefix = colorize(plugin.getConfig().getString("messages.prefix", "&8[&bEpicFly&8] &r"));
        String msg = plugin.getConfig().getString("messages." + key, "&cMesaj bulunamadı: " + key);
        return prefix + colorize(msg);
    }

    public String getRawMessage(String key) {
        return colorize(plugin.getConfig().getString("messages." + key, ""));
    }

    // Particles config section
    public ConfigurationSection getParticlesSection() {
        return null; // handled via getConfig().getList("particles")
    }

    public List<?> getParticlesList() {
        return plugin.getConfig().getList("particles", new ArrayList<>());
    }

    public List<?> getOpenSoundsList() {
        return plugin.getConfig().getList("sounds.fly-open", new ArrayList<>());
    }

    public List<?> getCloseSoundsList() {
        return plugin.getConfig().getList("sounds.fly-close", new ArrayList<>());
    }

    public static String colorize(String text) {
        if (text == null) return "";
        return text.replace("&", "§");
    }
}
