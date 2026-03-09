package dev.execute.epicfly.managers;

import dev.execute.epicfly.EpicFly;
import dev.execute.epicfly.models.PlayerData;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class DatabaseManager {

    private final EpicFly plugin;
    private Connection connection;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DatabaseManager(EpicFly plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            connect();
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Veritabanı bağlantısı kurulamadı!", e);
        }
    }

    private void connect() throws SQLException {
        ConfigManager cfg = plugin.getConfigManager();
        if (cfg.getDatabaseType().equals("MYSQL")) {
            String url = "jdbc:mysql://" + cfg.getMysqlHost() + ":" + cfg.getMysqlPort()
                    + "/" + cfg.getMysqlDatabase() + "?useSSL=false&autoReconnect=true&characterEncoding=UTF-8";
            connection = DriverManager.getConnection(url, cfg.getMysqlUsername(), cfg.getMysqlPassword());
            plugin.getLogger().info("MySQL bağlantısı kuruldu.");
        } else {
            File dbFile = new File(plugin.getDataFolder(), "epicfly.db");
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("SQLite bağlantısı kuruldu.");
        }
    }

    private void createTables() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS epicfly_players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    remaining_seconds BIGINT DEFAULT 0,
                    total_seconds_flown BIGINT DEFAULT 0,
                    total_spent DOUBLE DEFAULT 0,
                    total_purchases INT DEFAULT 0,
                    auto_renew BOOLEAN DEFAULT FALSE,
                    particle_effect VARCHAR(64) DEFAULT 'NONE',
                    open_sound VARCHAR(64) DEFAULT 'ENTITY_PLAYER_LEVELUP',
                    close_sound VARCHAR(64) DEFAULT 'ENTITY_GENERIC_BIG_FALL'
                )
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void savePlayer(PlayerData data) {
        executor.submit(() -> {
            try {
                ensureConnection();
                String sql = """
                        INSERT INTO epicfly_players
                        (uuid, remaining_seconds, total_seconds_flown, total_spent, total_purchases, auto_renew, particle_effect, open_sound, close_sound)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT(uuid) DO UPDATE SET
                            remaining_seconds = excluded.remaining_seconds,
                            total_seconds_flown = excluded.total_seconds_flown,
                            total_spent = excluded.total_spent,
                            total_purchases = excluded.total_purchases,
                            auto_renew = excluded.auto_renew,
                            particle_effect = excluded.particle_effect,
                            open_sound = excluded.open_sound,
                            close_sound = excluded.close_sound
                        """;
                // MySQL uses different upsert syntax
                if (plugin.getConfigManager().getDatabaseType().equals("MYSQL")) {
                    sql = """
                            INSERT INTO epicfly_players
                            (uuid, remaining_seconds, total_seconds_flown, total_spent, total_purchases, auto_renew, particle_effect, open_sound, close_sound)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                            ON DUPLICATE KEY UPDATE
                                remaining_seconds = VALUES(remaining_seconds),
                                total_seconds_flown = VALUES(total_seconds_flown),
                                total_spent = VALUES(total_spent),
                                total_purchases = VALUES(total_purchases),
                                auto_renew = VALUES(auto_renew),
                                particle_effect = VALUES(particle_effect),
                                open_sound = VALUES(open_sound),
                                close_sound = VALUES(close_sound)
                            """;
                }
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, data.getUuid().toString());
                    ps.setLong(2, data.getRemainingSeconds());
                    ps.setLong(3, data.getTotalSecondsFlown());
                    ps.setDouble(4, data.getTotalSpent());
                    ps.setInt(5, data.getTotalPurchases());
                    ps.setBoolean(6, data.isAutoRenew());
                    ps.setString(7, data.getParticleEffect());
                    ps.setString(8, data.getOpenSound());
                    ps.setString(9, data.getCloseSound());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Oyuncu verisi kaydedilemedi: " + data.getUuid(), e);
            }
        });
    }

    public PlayerData loadPlayer(UUID uuid) {
        try {
            ensureConnection();
            String sql = "SELECT * FROM epicfly_players WHERE uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        PlayerData data = new PlayerData(uuid);
                        data.setRemainingSeconds(rs.getLong("remaining_seconds"));
                        data.addFlownSecond(); // will be properly set below
                        // Reset and set proper value
                        PlayerData fresh = new PlayerData(uuid);
                        fresh.setRemainingSeconds(rs.getLong("remaining_seconds"));
                        // Manually set via reflection workaround - use direct fields via constructor
                        return buildFromResultSet(uuid, rs);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Oyuncu verisi yüklenemedi: " + uuid, e);
        }
        return new PlayerData(uuid);
    }

    private PlayerData buildFromResultSet(UUID uuid, ResultSet rs) throws SQLException {
        PlayerData data = new PlayerData(uuid);
        data.setRemainingSeconds(rs.getLong("remaining_seconds"));
        // total_seconds_flown - we need a setter
        long tsf = rs.getLong("total_seconds_flown");
        for (long i = 0; i < tsf; i++) data.addFlownSecond();
        data.addSpent(rs.getDouble("total_spent"));
        int purchases = rs.getInt("total_purchases");
        for (int i = 0; i < purchases; i++) data.incrementPurchases();
        data.setAutoRenew(rs.getBoolean("auto_renew"));
        data.setParticleEffect(rs.getString("particle_effect"));
        data.setOpenSound(rs.getString("open_sound"));
        data.setCloseSound(rs.getString("close_sound"));
        return data;
    }

    public List<PlayerData> getTopPlayers(int limit) {
        List<PlayerData> list = new ArrayList<>();
        try {
            ensureConnection();
            String sql = "SELECT * FROM epicfly_players ORDER BY total_seconds_flown DESC LIMIT ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        list.add(buildFromResultSet(uuid, rs));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Leaderboard yüklenemedi!", e);
        }
        return list;
    }

    private void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
    }

    public void close() {
        executor.shutdown();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Veritabanı kapatılırken hata!", e);
        }
    }
}
