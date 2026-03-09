package dev.execute.epicfly.listeners;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.models.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class DoubleJumpListener implements Listener {

    private final EpicFly plugin;

    public DoubleJumpListener(EpicFly plugin) {
        this.plugin = plugin;
    }

    // When a player double-taps jump (space), Bukkit fires PlayerToggleFlightEvent
    // We intercept this to activate our fly system
    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE
                || e.getPlayer().getGameMode() == GameMode.SPECTATOR) return;

        if (!e.isFlying()) return; // Only trigger when starting flight

        PlayerData data = plugin.getPlayerDataManager().get(e.getPlayer().getUniqueId());

        if (data.isFlyActive()) {
            // Already active through our system, allow
            return;
        }

        // Cancel the default and try to enable through our system
        e.setCancelled(true);

        if (e.getPlayer().hasPermission("epicfly.unlimited")) {
            plugin.getFlyManager().enableFly(e.getPlayer());
            return;
        }

        if (data.hasTime()) {
            plugin.getFlyManager().enableFly(e.getPlayer());
        } else {
            e.getPlayer().sendMessage(plugin.getConfigManager().getMessage("fly-no-time"));
            e.getPlayer().setAllowFlight(false);
        }
    }
}
