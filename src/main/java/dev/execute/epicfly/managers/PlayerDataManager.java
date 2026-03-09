package dev.execute.epicfly.managers;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.models.PlayerData;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final EpicFly plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public PlayerDataManager(EpicFly plugin) {
        this.plugin = plugin;
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> plugin.getDatabaseManager().loadPlayer(id));
    }

    public void load(UUID uuid) {
        cache.put(uuid, plugin.getDatabaseManager().loadPlayer(uuid));
    }

    public void save(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            plugin.getDatabaseManager().savePlayer(data);
        }
    }

    public void unload(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            plugin.getDatabaseManager().savePlayer(data);
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, PlayerData> entry : cache.entrySet()) {
            plugin.getDatabaseManager().savePlayer(entry.getValue());
        }
    }

    public boolean isLoaded(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public Collection<PlayerData> getAll() {
        return cache.values();
    }
}
