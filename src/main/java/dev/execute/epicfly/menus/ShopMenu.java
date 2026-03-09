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

public class ShopMenu implements InventoryHolder {

    private final EpicFly plugin;
    private final Player player;
    private final Inventory inventory;
    private final boolean discounted;
    private final int discountAmount;

    public ShopMenu(EpicFly plugin, Player player, boolean discounted, int discountAmount) {
        this.plugin = plugin;
        this.player = player;
        this.discounted = discounted;
        this.discountAmount = discountAmount;
        LanguageManager lang = plugin.getLanguageManager();
        this.inventory = Bukkit.createInventory(this, 54,
                LegacyComponentSerializer.legacySection().deserialize(lang.get("shop-title")));
        build();
    }

    private void build() {
        LanguageManager lang = plugin.getLanguageManager();
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").build();
        for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        String currency = plugin.getConfigManager().getCurrencySymbol();

        ItemStack info = new ItemBuilder(Material.CLOCK)
                .name(lang.get("shop-info-name"))
                .lore(List.of(
                        lang.get("shop-info-remaining", "{time}", data.hasTime() ? data.getFormattedRemaining() : "§cNone"),
                        lang.get("shop-info-total", "{total}", data.getFormattedTotalFlown()),
                        lang.get("shop-info-spent", "{spent}", currency + String.format("%.2f", data.getTotalSpent())),
                        lang.get("shop-info-purchases", "{count}", String.valueOf(data.getTotalPurchases()))
                )).build();
        inventory.setItem(4, info);

        if (discounted) {
            String amt = String.valueOf(discountAmount);
            ItemStack banner = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                    .name(lang.get("discount-banner-name"))
                    .lore(List.of(
                            lang.get("discount-banner-lore1", "{amount}", amt),
                            lang.get("discount-banner-lore2")
                    )).glow().build();
            for (int slot : new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53})
                inventory.setItem(slot, banner);
        }

        for (FlyPackage pkg : plugin.getConfigManager().getPackages()) {
            double price = discounted ? pkg.getDiscountedPrice(discountAmount) : pkg.getPrice();
            String priceFormatted = currency + String.format("%.2f", price);
            List<String> lore = new ArrayList<>();
            for (String line : pkg.getLore()) lore.add(line.replace("{price}", priceFormatted));
            if (discounted) {
                lore.add(lang.get("discount-banner-lore1", "{amount}", String.valueOf(discountAmount)));
                lore.add("§7" + currency + String.format("%.2f", pkg.getPrice()));
            }
            inventory.setItem(pkg.getSlot(), new ItemBuilder(pkg.getMaterial()).name(pkg.getName()).lore(lore).build());
        }

        inventory.setItem(8, new ItemBuilder(Material.COMPASS)
                .name(lang.get("btn-settings"))
                .lore(List.of(lang.get("btn-settings-lore"))).build());

        if (!discounted) {
            inventory.setItem(49, new ItemBuilder(Material.BARRIER)
                    .name(lang.get("btn-close")).lore(List.of()).build());
        }
    }

    public void handleClick(InventoryClickEvent e) {
        if (!e.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot == 49) { player.closeInventory(); return; }
        if (slot == 8) {
            player.closeInventory();
            new SettingsMenu(plugin, player).open();
            return;
        }
        for (FlyPackage pkg : plugin.getConfigManager().getPackages()) {
            if (pkg.getSlot() == slot) {
                player.closeInventory();
                double price = discounted ? pkg.getDiscountedPrice(discountAmount) : pkg.getPrice();
                new ConfirmMenu(plugin, player, pkg, price, discounted, discountAmount).open();
                return;
            }
        }
    }

    public void open() { player.openInventory(inventory); }

    @Override
    public Inventory getInventory() { return inventory; }
}
