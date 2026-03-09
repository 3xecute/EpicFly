package dev.execute.epicfly.listeners;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.menus.ConfirmMenu;
import dev.execute.epicfly.menus.SettingsMenu;
import dev.execute.epicfly.menus.ShopMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MenuListener implements Listener {

    private final EpicFly plugin;

    public MenuListener(EpicFly plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        if (e.getInventory().getHolder() instanceof ShopMenu menu) {
            menu.handleClick(e);
        } else if (e.getInventory().getHolder() instanceof ConfirmMenu menu) {
            menu.handleClick(e);
        } else if (e.getInventory().getHolder() instanceof SettingsMenu menu) {
            menu.handleClick(e);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        // future use
    }
}
