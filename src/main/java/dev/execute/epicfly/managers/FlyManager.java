package dev.execute.epicfly.managers;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.managers.LanguageManager;
import dev.execute.epicfly.models.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FlyManager {

    private final EpicFly plugin;
    private BukkitTask tickTask;
    private BukkitTask particleTask;
    private final Set<UUID> warnedPlayers = new HashSet<>();

    public FlyManager(EpicFly plugin) {
        this.plugin = plugin;
        startTasks();
    }

    private void startTasks() {
        // Main tick task - every second
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
                    if (data == null) continue;

                    // Combat tag check
                    if (data.isInCombat() && System.currentTimeMillis() > data.getCombatEndTime()) {
                        data.setInCombat(false);
                    }

                    // AFK check
                    if (plugin.getConfigManager().isAfkEnabled()) {
                        long afkMs = plugin.getConfigManager().getAfkTimeout() * 1000L;
                        boolean isAfk = (System.currentTimeMillis() - data.getLastMovement()) > afkMs;
                        if (isAfk && !data.isAfk()) {
                            data.setAfk(true);
                            if (data.isFlyActive()) {
                                player.sendMessage(plugin.getLanguageManager().withPrefix("afk-pause"));
                            }
                        } else if (!isAfk && data.isAfk()) {
                            data.setAfk(false);
                            if (data.isFlyActive()) {
                                player.sendMessage(plugin.getLanguageManager().withPrefix("afk-resume"));
                            }
                        }
                    }

                    if (!data.isFlyActive()) continue;

                    // Don't tick if AFK or unlimited
                    if (data.isAfk()) continue;
                    if (player.hasPermission("epicfly.unlimited")) {
                        updateActionBar(player, data, true);
                        continue;
                    }

                    // Only subtract time when player is actually flying in the air
                    if (!player.isFlying()) continue;

                    // Subtract time
                    data.subtractSecond();
                    data.addFlownSecond();

                    long remaining = data.getRemainingSeconds();

                    // Update actionbar
                    updateActionBar(player, data, false);

                    // Warn before end
                    int warnAt = plugin.getConfigManager().getWarnBeforeEnd();
                    if (remaining <= warnAt && remaining > 0 && !warnedPlayers.contains(player.getUniqueId())) {
                        warnedPlayers.add(player.getUniqueId());
                        sendExtendMessage(player);
                    }

                    // Auto renew check
                    if (remaining <= 5 && data.isAutoRenew()) {
                        tryAutoRenew(player, data);
                    }

                    // Expired
                    if (remaining <= 0) {
                        warnedPlayers.remove(player.getUniqueId());
                        expireFly(player, data);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        // Particle task - every 5 ticks
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
                    if (data == null || !data.isFlyActive()) continue;
                    spawnParticle(player, data);
                }
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    private void updateActionBar(Player player, PlayerData data, boolean unlimited) {
        String text;
        if (unlimited) {
            text = "§b✈ §fSınırsız Uçuş";
        } else {
            long remaining = data.getRemainingSeconds();
            String color = remaining > 60 ? "§a" : remaining > 15 ? "§e" : "§c";
            text = "§b✈ §7Kalan: " + color + data.getFormattedRemaining();
            if (data.isAfk()) text += " §7(Duraklatıldı)";
        }
        player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(text));
    }

    private void sendExtendMessage(Player player) {
        String baseMsg = plugin.getLanguageManager().get("extend-message");

        TextComponent message = Component.text()
                .append(LegacyComponentSerializer.legacySection().deserialize(baseMsg))
                .clickEvent(ClickEvent.runCommand("/efly extend"))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                        Component.text("Uçuş sürenizi uzatmak için tıklayın!", NamedTextColor.YELLOW)))
                .build();

        player.sendMessage(message);
    }

    private void tryAutoRenew(Player player, PlayerData data) {
        if (plugin.getEconomy() == null) return;
        var packages = plugin.getConfigManager().getPackages();
        if (packages.isEmpty()) return;

        var cheapest = packages.stream()
                .min(java.util.Comparator.comparingDouble(p -> p.getPrice()))
                .orElse(null);
        if (cheapest == null) return;

        if (plugin.getEconomy().has(player, cheapest.getPrice())) {
            plugin.getEconomy().withdrawPlayer(player, cheapest.getPrice());
            data.addSeconds(cheapest.getDurationSeconds());
            data.addSpent(cheapest.getPrice());
            data.incrementPurchases();
            player.sendMessage(plugin.getLanguageManager().withPrefix("purchase-success")
                    .replace("{duration}", String.valueOf(cheapest.getDurationMinutes()))
                    .replace("{price}", plugin.getConfigManager().getCurrencySymbol() + cheapest.getPrice()));
        }
    }

    private void expireFly(Player player, PlayerData data) {
        data.setFlyActive(false);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.sendMessage(plugin.getLanguageManager().withPrefix("fly-expired"));
        playSound(player, data.getCloseSound());

        if (plugin.getConfigManager().isSlowFallEnabled()) {
            int duration = plugin.getConfigManager().getSlowFallDuration() * 20;
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration, 0, false, false));
        }
    }

    public boolean enableFly(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());

        // World check
        if (!player.hasPermission("epicfly.bypass.world")) {
            if (plugin.getConfigManager().getDisabledWorlds().contains(player.getWorld().getName())) {
                player.sendMessage(plugin.getLanguageManager().withPrefix("fly-disabled-world"));
                return false;
            }
        }

        // Combat check
        if (plugin.getConfigManager().isCombatEnabled() && data.isInCombat() && !player.hasPermission("epicfly.bypass.combat")) {
            player.sendMessage(plugin.getLanguageManager().withPrefix("combat-tag"));
            return false;
        }

        // Unlimited
        if (player.hasPermission("epicfly.unlimited")) {
            data.setFlyActive(true);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(plugin.getLanguageManager().withPrefix("fly-enabled"));
            playSound(player, data.getOpenSound());
            return true;
        }

        if (!data.hasTime()) {
            player.sendMessage(plugin.getLanguageManager().withPrefix("fly-no-time"));
            return false;
        }

        data.setFlyActive(true);
        data.setFlyStartTime(System.currentTimeMillis());
        warnedPlayers.remove(player.getUniqueId());
        player.setAllowFlight(true);
        player.setFlying(true);
        player.sendMessage(plugin.getLanguageManager().withPrefix("fly-enabled"));
        playSound(player, data.getOpenSound());
        return true;
    }

    public void disableFly(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        data.setFlyActive(false);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.sendActionBar(Component.empty());
        player.sendMessage(plugin.getLanguageManager().withPrefix("fly-disabled"));
        playSound(player, data.getCloseSound());
    }

    public void toggleFly(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (data.isFlyActive()) {
            disableFly(player);
        } else {
            enableFly(player);
        }
    }

    private void spawnParticle(Player player, PlayerData data) {
        String effect = data.getParticleEffect();
        if (effect == null || effect.equals("NONE")) return;
        try {
            Particle particle = Particle.valueOf(effect);
            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 0.5, 0), 5, 0.3, 0.3, 0.3, 0);
        } catch (Exception ignored) {}
    }

    private void playSound(Player player, String soundName) {
        if (soundName == null || soundName.equals("NONE")) return;
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }

    public void disableFlyForCombat(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (data.isFlyActive()) {
            data.setFlyActive(false);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage(plugin.getLanguageManager().withPrefix("combat-tag"));
            if (plugin.getConfigManager().isSlowFallEnabled()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, false, false));
            }
        }
    }

    public void saveAll() {
        plugin.getPlayerDataManager().saveAll();
    }

    public void reload() {
        warnedPlayers.clear();
    }
}
