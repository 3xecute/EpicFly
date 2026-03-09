package dev.execute.epicfly.listeners;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.models.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private final EpicFly plugin;

    public PlayerListener(EpicFly plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getPlayerDataManager().load(e.getPlayer().getUniqueId());

        // If they were flying before, restore
        PlayerData data = plugin.getPlayerDataManager().get(e.getPlayer().getUniqueId());
        if (data.isFlyActive() && data.hasTime()) {
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE && e.getPlayer().getGameMode() != GameMode.SPECTATOR) {
                plugin.getFlyManager().enableFly(e.getPlayer());
            }
        } else {
            data.setFlyActive(false);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PlayerData data = plugin.getPlayerDataManager().get(e.getPlayer().getUniqueId());
        if (data != null && data.isFlyActive()) {
            e.getPlayer().setAllowFlight(false);
            e.getPlayer().setFlying(false);
            data.setFlyActive(false);
        }
        plugin.getPlayerDataManager().unload(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!e.hasChangedPosition()) return;
        PlayerData data = plugin.getPlayerDataManager().get(e.getPlayer().getUniqueId());
        if (data != null) {
            data.setLastMovement(System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        PlayerData data = plugin.getPlayerDataManager().get(e.getPlayer().getUniqueId());
        if (data != null && data.isFlyActive()) {
            String worldName = e.getPlayer().getWorld().getName();
            if (plugin.getConfigManager().getDisabledWorlds().contains(worldName)
                    && !e.getPlayer().hasPermission("epicfly.bypass.world")) {
                plugin.getFlyManager().disableFly(e.getPlayer());
                e.getPlayer().sendMessage(plugin.getLanguageManager().withPrefix("fly-disabled-world"));
            }
        }
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        // If switching to creative/spectator, let vanilla handle fly
        // If switching away from creative/spectator and fly was active, restore
        PlayerData data = plugin.getPlayerDataManager().get(e.getPlayer().getUniqueId());
        if (data == null) return;

        switch (e.getNewGameMode()) {
            case CREATIVE, SPECTATOR -> {
                // Don't interfere
            }
            case SURVIVAL, ADVENTURE -> {
                // After gamemode change, apply our fly state
                if (!data.isFlyActive() || !data.hasTime()) {
                    e.getPlayer().setAllowFlight(false);
                }
            }
        }
    }
}
