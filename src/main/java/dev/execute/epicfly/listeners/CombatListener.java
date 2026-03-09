package dev.execute.epicfly.listeners;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {

    private final EpicFly plugin;

    public CombatListener(EpicFly plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent e) {
        if (!plugin.getConfigManager().isCombatEnabled()) return;

        Player attacker = null;
        Player victim = null;

        if (e.getDamager() instanceof Player p) attacker = p;
        if (e.getEntity() instanceof Player p) victim = p;

        if (attacker == null && victim == null) return;

        int combatSeconds = plugin.getConfigManager().getCombatDuration();
        long combatEndTime = System.currentTimeMillis() + (combatSeconds * 1000L);

        if (attacker != null) tagCombat(attacker, combatEndTime);
        if (victim != null) tagCombat(victim, combatEndTime);
    }

    private void tagCombat(Player player, long endTime) {
        if (player.hasPermission("epicfly.bypass.combat")) return;

        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        data.setInCombat(true);
        data.setCombatEndTime(endTime);

        if (data.isFlyActive()) {
            plugin.getFlyManager().disableFlyForCombat(player);
        }
    }
}
