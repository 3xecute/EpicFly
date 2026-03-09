package dev.execute.epicfly;

import dev.execute.epicfly.commands.EFlyCommand;
import dev.execute.epicfly.listeners.*;
import dev.execute.epicfly.managers.*;
import dev.execute.epicfly.placeholders.EpicFlyPlaceholder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EpicFly extends JavaPlugin {

    private static EpicFly instance;
    private Economy economy;
    private DatabaseManager databaseManager;
    private FlyManager flyManager;
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private LeaderboardManager leaderboardManager;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this);

        // Database
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        // Player data
        playerDataManager = new PlayerDataManager(this);

        // Vault
        if (!setupEconomy()) {
            getLogger().warning("Vault bulunamadı! Ekonomi özellikleri devre dışı.");
        }

        // Fly manager
        flyManager = new FlyManager(this);

        // Leaderboard
        leaderboardManager = new LeaderboardManager(this);

        // Commands
        EFlyCommand eflyCommand = new EFlyCommand(this);
        getCommand("efly").setExecutor(eflyCommand);
        getCommand("efly").setTabCompleter(eflyCommand);

        // Listeners
        getServer().getPluginManager().registerEvents(new FlyListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new DoubleJumpListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EpicFlyPlaceholder(this).register();
            getLogger().info("PlaceholderAPI hook başarılı!");
        }

        getLogger().info("EpicFly v" + getDescription().getVersion() + " aktif edildi!");
    }

    @Override
    public void onDisable() {
        if (flyManager != null) {
            flyManager.saveAll();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("EpicFly devre dışı bırakıldı.");
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        languageManager.load();
        flyManager.reload();
        leaderboardManager.reload();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public static EpicFly getInstance() { return instance; }
    public Economy getEconomy() { return economy; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public FlyManager getFlyManager() { return flyManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public LeaderboardManager getLeaderboardManager() { return leaderboardManager; }
    public LanguageManager getLanguageManager() { return languageManager; }
}
