package dev.execute.epicfly.listeners;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.models.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class FlyListener implements Listener {

    private final EpicFly plugin;

    public FlyListener(EpicFly plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onToggleFlight(PlayerToggleFlightEvent e) {
        // Allow creative/spectator
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE
                || e.getPlayer().getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = plugin.getPlayerDataManager().get(e.getPlayer().getUniqueId());

        // If flight is being enabled but not through our system, check
        if (e.isFlying() && !data.isFlyActive() && !e.getPlayer().hasPermission("epicfly.unlimited")) {
            e.setCancelled(true);
        }
    }
}
