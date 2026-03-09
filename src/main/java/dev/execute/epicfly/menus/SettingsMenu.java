package dev.execute.epicfly.menus;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.managers.ConfigManager;
import dev.execute.epicfly.managers.LanguageManager;
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
import java.util.Map;

public class SettingsMenu implements InventoryHolder {

    private final EpicFly plugin;
    private final Player player;
    private Inventory inventory;
    private int page;

    public SettingsMenu(EpicFly plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.page = 0;
    }

    public void open() { buildMain(); player.openInventory(inventory); }

    private void buildMain() {
        page = 0;
        LanguageManager lang = plugin.getLanguageManager();
        inventory = Bukkit.createInventory(this, 27,
                LegacyComponentSerializer.legacySection().deserialize(lang.get("settings-title")));
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").build();
        for (int i = 0; i < 27; i++) inventory.setItem(i, filler);

        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());

        inventory.setItem(11, new ItemBuilder(Material.BLAZE_POWDER)
                .name(lang.get("settings-particle-name"))
                .lore(List.of(lang.get("settings-particle-lore1", "{current}", data.getParticleEffect()), "", lang.get("settings-particle-lore2"))).build());

        inventory.setItem(13, new ItemBuilder(Material.NOTE_BLOCK)
                .name(lang.get("settings-open-sound-name"))
                .lore(List.of(lang.get("settings-open-sound-lore1", "{current}", data.getOpenSound()), "", lang.get("settings-open-sound-lore2"))).build());

        inventory.setItem(15, new ItemBuilder(Material.NOTE_BLOCK)
                .name(lang.get("settings-close-sound-name"))
                .lore(List.of(lang.get("settings-close-sound-lore1", "{current}", data.getCloseSound()), "", lang.get("settings-close-sound-lore2"))).build());

        boolean autoRenew = data.isAutoRenew();
        inventory.setItem(22, new ItemBuilder(autoRenew ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(lang.get(autoRenew ? "settings-autorenew-name-on" : "settings-autorenew-name-off"))
                .lore(List.of(
                        lang.get(autoRenew ? "settings-autorenew-status-on" : "settings-autorenew-status-off"),
                        "",
                        lang.get("settings-autorenew-desc1"),
                        lang.get("settings-autorenew-desc2"),
                        "",
                        lang.get("settings-autorenew-click")
                )).build());

        inventory.setItem(0, new ItemBuilder(Material.ARROW).name(lang.get("btn-back-shop")).build());
    }

    @SuppressWarnings("unchecked")
    private void buildParticles() {
        page = 1;
        LanguageManager lang = plugin.getLanguageManager();
        inventory = Bukkit.createInventory(this, 54,
                LegacyComponentSerializer.legacySection().deserialize(lang.get("particle-title")));
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").build();
        for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        List<?> particles = plugin.getConfigManager().getParticlesList();
        int slot = 10;
        for (Object obj : particles) {
            if (!(obj instanceof Map)) continue;
            Map<String, Object> map = (Map<String, Object>) obj;
            String name = ConfigManager.colorize((String) map.get("name"));
            String effect = (String) map.get("effect");
            String matStr = map.containsKey("material") ? (String) map.get("material") : "FEATHER";
            Material mat = Material.matchMaterial(matStr);
            if (mat == null) mat = Material.FEATHER;
            boolean selected = effect.equals(data.getParticleEffect());
            List<String> lore = new ArrayList<>(List.of("&7Effect: &f" + effect, "", selected ? lang.get("item-selected") : lang.get("item-click-select")));
            ItemBuilder b = new ItemBuilder(mat).name(name).lore(lore);
            if (selected) b.glow();
            inventory.setItem(slot, b.build());
            slot++;
            if (slot % 9 == 8) slot += 2;
            if (slot >= 44) break;
        }
        inventory.setItem(45, new ItemBuilder(Material.ARROW).name(lang.get("btn-back")).build());
    }

    @SuppressWarnings("unchecked")
    private void buildSounds(boolean openSound) {
        page = openSound ? 2 : 3;
        LanguageManager lang = plugin.getLanguageManager();
        inventory = Bukkit.createInventory(this, 54,
                LegacyComponentSerializer.legacySection().deserialize(lang.get(openSound ? "open-sound-title" : "close-sound-title")));
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").build();
        for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        List<?> sounds = openSound ? plugin.getConfigManager().getOpenSoundsList() : plugin.getConfigManager().getCloseSoundsList();
        String current = openSound ? data.getOpenSound() : data.getCloseSound();
        int slot = 10;
        for (Object obj : sounds) {
            if (!(obj instanceof Map)) continue;
            Map<String, Object> map = (Map<String, Object>) obj;
            String name = ConfigManager.colorize((String) map.get("name"));
            String sound = (String) map.get("sound");
            boolean selected = sound.equals(current);
            List<String> lore = new ArrayList<>(List.of("&7Sound: &f" + sound, "", selected ? lang.get("item-selected") : lang.get("item-click-select")));
            ItemBuilder b = new ItemBuilder(Material.NOTE_BLOCK).name(name).lore(lore);
            if (selected) b.glow();
            inventory.setItem(slot, b.build());
            slot++;
            if (slot % 9 == 8) slot += 2;
            if (slot >= 44) break;
        }
        inventory.setItem(45, new ItemBuilder(Material.ARROW).name(lang.get("btn-back")).build());
    }

    @SuppressWarnings("unchecked")
    public void handleClick(InventoryClickEvent e) {
        if (!e.getWhoClicked().getUniqueId().equals(player.getUniqueId())) return;
        e.setCancelled(true);
        int slot = e.getRawSlot();
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        LanguageManager lang = plugin.getLanguageManager();

        if (page == 0) {
            switch (slot) {
                case 11 -> { buildParticles(); player.openInventory(inventory); }
                case 13 -> { buildSounds(true); player.openInventory(inventory); }
                case 15 -> { buildSounds(false); player.openInventory(inventory); }
                case 22 -> {
                    data.setAutoRenew(!data.isAutoRenew());
                    plugin.getPlayerDataManager().save(player.getUniqueId());
                    player.sendMessage(lang.withPrefix(data.isAutoRenew() ? "auto-renew-enabled" : "auto-renew-disabled"));
                    buildMain();
                    player.openInventory(inventory);
                }
                case 0 -> { player.closeInventory(); new ShopMenu(plugin, player, false, 0).open(); }
            }
        } else if (page == 1) {
            if (slot == 45) { buildMain(); player.openInventory(inventory); return; }
            List<?> particles = plugin.getConfigManager().getParticlesList();
            int itemSlot = 10;
            for (Object obj : particles) {
                if (!(obj instanceof Map)) continue;
                Map<String, Object> map = (Map<String, Object>) obj;
                if (itemSlot == slot) {
                    data.setParticleEffect((String) map.get("effect"));
                    plugin.getPlayerDataManager().save(player.getUniqueId());
                    buildParticles(); player.openInventory(inventory); return;
                }
                itemSlot++;
                if (itemSlot % 9 == 8) itemSlot += 2;
            }
        } else if (page == 2 || page == 3) {
            boolean isOpen = (page == 2);
            if (slot == 45) { buildMain(); player.openInventory(inventory); return; }
            List<?> sounds = isOpen ? plugin.getConfigManager().getOpenSoundsList() : plugin.getConfigManager().getCloseSoundsList();
            int itemSlot = 10;
            for (Object obj : sounds) {
                if (!(obj instanceof Map)) continue;
                Map<String, Object> map = (Map<String, Object>) obj;
                if (itemSlot == slot) {
                    String sound = (String) map.get("sound");
                    if (isOpen) data.setOpenSound(sound); else data.setCloseSound(sound);
                    plugin.getPlayerDataManager().save(player.getUniqueId());
                    buildSounds(isOpen); player.openInventory(inventory); return;
                }
                itemSlot++;
                if (itemSlot % 9 == 8) itemSlot += 2;
            }
        }
    }

    @Override
    public Inventory getInventory() { return inventory; }
}
