package dev.execute.epicfly.menus;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.managers.LanguageManager;
import dev.execute.epicfly.models.FlyPackage;
import dev.execute.epicfly.models.PlayerData;
import dev.execute.epicfly.utils.ItemBuilder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ConfirmMenu implements InventoryHolder {

    private final EpicFly plugin;
    private final Player player;
    private final FlyPackage flyPackage;
    private final double price;
    private final boolean discounted;
    private final int discountAmount;
    private final Inventory inventory;

    public ConfirmMenu(EpicFly plugin, Player player, FlyPackage flyPackage, double price, boolean discounted, int discountAmount) {
        this.plugin = plugin;
        this.player = player;
        this.flyPackage = flyPackage;
        this.price = price;
        this.discounted = discounted;
        this.discountAmount = discountAmount;
        LanguageManager lang = plugin.getLanguageManager();
        this.inventory = Bukkit.createInventory(this, 27,
                LegacyComponentSerializer.legacySection().deserialize(lang.get("confirm-title")));
        build();
    }

    private void build() {
        LanguageManager lang = plugin.getLanguageManager();
        String currency = plugin.getConfigManager().getCurrencySymbol();
        String priceStr = currency + String.format("%.2f", price);

        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").build();
        for (int i = 0; i < 27; i++) inventory.setItem(i, filler);

        List<String> pkgLore = new ArrayList<>();
        pkgLore.add(lang.get("confirm-pkg-lore-duration", "{duration}", flyPackage.getFormattedDuration()));
        pkgLore.add(lang.get("confirm-pkg-lore-price", "{price}", priceStr));
        if (discounted) pkgLore.add(lang.get("confirm-pkg-lore-discount", "{amount}", String.valueOf(discountAmount)));
        pkgLore.add("");
        pkgLore.add(lang.get("confirm-pkg-lore-hint"));

        inventory.setItem(13, new ItemBuilder(flyPackage.getMaterial())
                .name(flyPackage.getName()).lore(pkgLore).build());

        inventory.setItem(11, new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                .name(lang.get("btn-confirm"))
                .lore(List.of(
                        lang.get("btn-confirm-lore1", "{duration}", flyPackage.getFormattedDuration()),
                        lang.get("btn-confirm-lore2", "{price}", priceStr)
                )).build());

        inventory.setItem(15, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .name(lang.get("btn-cancel"))
                .lore(List.of(lang.get("btn-cancel-lore"))).build());
    }

    public void handleClick(InventoryClickEvent e) {
        if (!e.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot == 11) { player.closeInventory(); processPurchase(); }
        else if (slot == 15) { player.closeInventory(); player.sendMessage(plugin.getLanguageManager().withPrefix("purchase-cancel")); }
    }

    private void processPurchase() {
        LanguageManager lang = plugin.getLanguageManager();
        String currency = plugin.getConfigManager().getCurrencySymbol();
        if (plugin.getEconomy() == null) { player.sendMessage(lang.withPrefix("error-economy-disabled")); return; }
        if (!plugin.getEconomy().has(player, price)) {
            player.sendMessage(lang.withPrefix("not-enough-money", "{price}", currency + String.format("%.2f", price)));
            return;
        }
        plugin.getEconomy().withdrawPlayer(player, price);
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        data.addSeconds(flyPackage.getDurationSeconds());
        data.addSpent(price);
        data.incrementPurchases();
        plugin.getPlayerDataManager().save(player.getUniqueId());
        player.sendMessage(lang.withPrefix("purchase-success",
                "{duration}", String.valueOf(flyPackage.getDurationMinutes()),
                "{price}", currency + String.format("%.2f", price)));
        if (discounted) {
            player.sendMessage(lang.withPrefix("discount-applied", "{amount}", String.valueOf(discountAmount)));
        }
    }

    public void open() { player.openInventory(inventory); }

    @Override
    public Inventory getInventory() { return inventory; }
}
