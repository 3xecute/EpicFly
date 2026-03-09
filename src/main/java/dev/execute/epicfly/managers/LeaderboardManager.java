package dev.execute.epicfly.managers;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardManager {

    private final EpicFly plugin;
    private List<PlayerData> cachedLeaderboard = new ArrayList<>();
    private BukkitTask updateTask;

    public LeaderboardManager(EpicFly plugin) {
        this.plugin = plugin;
        if (plugin.getConfigManager().isLeaderboardEnabled()) {
            startUpdateTask();
        }
    }

    private void startUpdateTask() {
        if (updateTask != null) updateTask.cancel();
        int interval = plugin.getConfigManager().getLeaderboardUpdateInterval() * 20;
        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            cachedLeaderboard = plugin.getDatabaseManager().getTopPlayers(10);
        }, 20L, interval);
    }

    public List<PlayerData> getLeaderboard() {
        return cachedLeaderboard;
    }

    public void reload() {
        if (updateTask != null) updateTask.cancel();
        cachedLeaderboard.clear();
        if (plugin.getConfigManager().isLeaderboardEnabled()) {
            startUpdateTask();
        }
    }
}
