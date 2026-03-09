package dev.execute.epicfly.placeholders;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.models.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EpicFlyPlaceholder extends PlaceholderExpansion {

    private final EpicFly plugin;

    public EpicFlyPlaceholder(EpicFly plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "fly"; }

    @Override
    public @NotNull String getAuthor() { return "execute"; }

    @Override
    public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public boolean persist() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (data == null) return "";

        return switch (params.toLowerCase()) {
            case "remaining" -> data.getFormattedRemaining();
            case "remaining_seconds" -> String.valueOf(data.getRemainingSeconds());
            case "total_time" -> data.getFormattedTotalFlown();
            case "total_seconds" -> String.valueOf(data.getTotalSecondsFlown());
            case "is_active" -> data.isFlyActive() ? "✔" : "✘";
            case "is_active_bool" -> String.valueOf(data.isFlyActive());
            case "total_spent" -> plugin.getConfigManager().getCurrencySymbol() + String.format("%.2f", data.getTotalSpent());
            case "purchases" -> String.valueOf(data.getTotalPurchases());
            case "auto_renew" -> data.isAutoRenew() ? "Aktif" : "Devre Dışı";
            case "particle" -> data.getParticleEffect();
            case "in_combat" -> data.isInCombat() ? "✔" : "✘";
            case "is_afk" -> data.isAfk() ? "✔" : "✘";
            case "has_unlimited" -> player.hasPermission("epicfly.unlimited") ? "✔" : "✘";
            default -> null;
        };
    }
}
