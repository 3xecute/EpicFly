package dev.execute.epicfly.models;

import org.bukkit.Material;

import java.util.List;

public class FlyPackage {

    private final int id;
    private final String name;
    private final List<String> lore;
    private final Material material;
    private final long durationMinutes;
    private final double price;
    private final int slot;

    public FlyPackage(int id, String name, List<String> lore, Material material, long durationMinutes, double price, int slot) {
        this.id = id;
        this.name = name;
        this.lore = lore;
        this.material = material;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.slot = slot;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public List<String> getLore() { return lore; }
    public Material getMaterial() { return material; }
    public long getDurationMinutes() { return durationMinutes; }
    public long getDurationSeconds() { return durationMinutes * 60; }
    public double getPrice() { return price; }
    public int getSlot() { return slot; }

    public double getDiscountedPrice(int discountPercent) {
        return price * (1.0 - discountPercent / 100.0);
    }

    public String getFormattedDuration() {
        if (durationMinutes >= 1440) {
            return (durationMinutes / 1440) + " gün";
        } else if (durationMinutes >= 60) {
            return (durationMinutes / 60) + " saat";
        } else {
            return durationMinutes + " dakika";
        }
    }
}
