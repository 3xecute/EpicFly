package dev.execute.epicfly.commands;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.managers.LanguageManager;
import dev.execute.epicfly.menus.ShopMenu;
import dev.execute.epicfly.models.PlayerData;
import dev.execute.epicfly.utils.TimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EFlyCommand implements CommandExecutor, TabCompleter {

    private final EpicFly plugin;
    private final Random random = new Random();

    public EFlyCommand(EpicFly plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        LanguageManager lang = plugin.getLanguageManager();

        if (!player.hasPermission("epicfly.use")) {
            player.sendMessage(lang.withPrefix("no-permission"));
            return true;
        }

        if (args.length == 0) {
            new ShopMenu(plugin, player, false, 0).open();
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "fly", "toggle" -> plugin.getFlyManager().toggleFly(player);

            case "extend" -> handleExtend(player);

            case "give" -> {
                if (!player.hasPermission("epicfly.give")) { player.sendMessage(lang.withPrefix("no-permission")); return true; }
                if (args.length < 3) { player.sendMessage(lang.getPrefix() + "&c/efly give <player> <duration>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { player.sendMessage(lang.withPrefix("error-player-not-found")); return true; }
                long seconds = TimeUtils.parseTime(args[2]);
                if (seconds <= 0) { player.sendMessage(lang.withPrefix("error-invalid-duration")); return true; }
                PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
                data.addSeconds(seconds);
                plugin.getPlayerDataManager().save(target.getUniqueId());
                player.sendMessage(lang.withPrefix("fly-given", "{player}", target.getName(), "{duration}", TimeUtils.formatSeconds(seconds)));
                target.sendMessage(lang.withPrefix("fly-received", "{duration}", TimeUtils.formatSeconds(seconds)));
            }

            case "list" -> {
                if (!player.hasPermission("epicfly.list")) { player.sendMessage(lang.withPrefix("no-permission")); return true; }
                List<String> flying = new ArrayList<>();
                for (Player online : Bukkit.getOnlinePlayers()) {
                    PlayerData d = plugin.getPlayerDataManager().get(online.getUniqueId());
                    if (d != null && d.isFlyActive()) flying.add(online.getName() + " (" + d.getFormattedRemaining() + ")");
                }
                player.sendMessage(lang.withPrefix("list-header", "{count}", String.valueOf(flying.size())));
                if (flying.isEmpty()) {
                    player.sendMessage(lang.get("list-empty"));
                } else {
                    flying.forEach(s -> player.sendMessage(lang.get("list-entry", "{player}", s.split(" ")[0], "{time}", s.contains("(") ? s.split("\\(")[1].replace(")", "") : "")));
                }
            }

            case "stats" -> {
                Player target = args.length >= 2 ? Bukkit.getPlayer(args[1]) : player;
                if (target == null) { player.sendMessage(lang.withPrefix("error-player-not-found")); return true; }
                PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
                String currency = plugin.getConfigManager().getCurrencySymbol();
                player.sendMessage(lang.get("stats-header", "{player}", target.getName()));
                player.sendMessage(lang.get("stats-remaining", "{time}", data.getFormattedRemaining()));
                player.sendMessage(lang.get("stats-total", "{total}", data.getFormattedTotalFlown()));
                player.sendMessage(lang.get("stats-spent", "{spent}", currency + String.format("%.2f", data.getTotalSpent())));
                player.sendMessage(lang.get("stats-purchases", "{count}", String.valueOf(data.getTotalPurchases())));
                player.sendMessage(lang.get("stats-autorenew", "{status}", lang.get(data.isAutoRenew() ? "stats-autorenew-on" : "stats-autorenew-off")));
            }

            case "top", "leaderboard" -> {
                if (!plugin.getConfigManager().isLeaderboardEnabled()) { player.sendMessage(lang.withPrefix("top-disabled")); return true; }
                var board = plugin.getLeaderboardManager().getLeaderboard();
                player.sendMessage(lang.get("top-header"));
                if (board.isEmpty()) {
                    player.sendMessage(lang.get("top-empty"));
                } else {
                    int rank = 1;
                    for (var d : board) {
                        String name = Bukkit.getOfflinePlayer(d.getUuid()).getName();
                        if (name == null) name = d.getUuid().toString().substring(0, 8);
                        player.sendMessage(lang.get("top-entry", "{rank}", String.valueOf(rank), "{player}", name, "{time}", d.getFormattedTotalFlown()));
                        rank++;
                    }
                }
            }

            case "reload" -> {
                if (!player.hasPermission("epicfly.reload")) { player.sendMessage(lang.withPrefix("no-permission")); return true; }
                plugin.reload();
                player.sendMessage(plugin.getLanguageManager().withPrefix("plugin-reloaded"));
            }

            default -> sendHelp(player);
        }

        return true;
    }

    private void handleExtend(Player player) {
        double chance = plugin.getConfigManager().getDiscountChance();
        boolean discounted = plugin.getConfigManager().isDiscountEnabled() && (random.nextDouble() * 100) < chance;
        int discountAmount = discounted ? plugin.getConfigManager().getDiscountAmount() : 0;
        if (discounted) {
            player.sendMessage(plugin.getLanguageManager().withPrefix("discount-applied", "{amount}", String.valueOf(discountAmount)));
        }
        new ShopMenu(plugin, player, discounted, discountAmount).open();
    }

    private void sendHelp(Player player) {
        LanguageManager lang = plugin.getLanguageManager();
        player.sendMessage(lang.get("help-header"));
        player.sendMessage(lang.get("help-title"));
        player.sendMessage(lang.get("help-header"));
        player.sendMessage(lang.get("help-efly"));
        player.sendMessage(lang.get("help-fly"));
        player.sendMessage(lang.get("help-stats"));
        player.sendMessage(lang.get("help-top"));
        if (player.hasPermission("epicfly.give")) player.sendMessage(lang.get("help-give"));
        if (player.hasPermission("epicfly.list")) player.sendMessage(lang.get("help-list"));
        if (player.hasPermission("epicfly.reload")) player.sendMessage(lang.get("help-reload"));
        player.sendMessage(lang.get("help-header"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(List.of("fly", "stats", "top"));
            if (sender.hasPermission("epicfly.give")) completions.add("give");
            if (sender.hasPermission("epicfly.list")) completions.add("list");
            if (sender.hasPermission("epicfly.reload")) completions.add("reload");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("stats"))) {
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(List.of("30m", "1h", "3h", "1d", "7d"));
        }
        return completions;
    }
}
