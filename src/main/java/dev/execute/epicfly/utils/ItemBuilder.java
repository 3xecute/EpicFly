package dev.execute.epicfly.utils;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        meta.displayName(LegacyComponentSerializer.legacySection().deserialize(name));
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        meta.lore(lore.stream()
                .map(l -> LegacyComponentSerializer.legacySection().deserialize(l))
                .collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder glow() {
        Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
        if (ench != null) {
            meta.addEnchant(ench, 1, true);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
