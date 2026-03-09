package dev.execute.epicfly.models;

import org.bukkit.Particle;
import org.bukkit.Sound;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private long remainingSeconds;
    private long totalSecondsFlown;
    private double totalSpent;
    private int totalPurchases;
    private boolean autoRenew;
    private boolean flyActive;
    private String particleEffect;
    private String openSound;
    private String closeSound;

    // Runtime only (not saved to DB)
    private boolean afk;
    private boolean inCombat;
    private long combatEndTime;
    private long lastMovement;
    private long flyStartTime;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.remainingSeconds = 0;
        this.totalSecondsFlown = 0;
        this.totalSpent = 0;
        this.totalPurchases = 0;
        this.autoRenew = false;
        this.flyActive = false;
        this.particleEffect = "NONE";
        this.openSound = "ENTITY_PLAYER_LEVELUP";
        this.closeSound = "ENTITY_GENERIC_BIG_FALL";
        this.lastMovement = System.currentTimeMillis();
    }

    public UUID getUuid() { return uuid; }

    public long getRemainingSeconds() { return remainingSeconds; }
    public void setRemainingSeconds(long seconds) { this.remainingSeconds = Math.max(0, seconds); }
    public void addSeconds(long seconds) { this.remainingSeconds += seconds; }
    public void subtractSecond() { this.remainingSeconds = Math.max(0, remainingSeconds - 1); }

    public long getTotalSecondsFlown() { return totalSecondsFlown; }
    public void addFlownSecond() { this.totalSecondsFlown++; }

    public double getTotalSpent() { return totalSpent; }
    public void addSpent(double amount) { this.totalSpent += amount; }

    public int getTotalPurchases() { return totalPurchases; }
    public void incrementPurchases() { this.totalPurchases++; }

    public boolean isAutoRenew() { return autoRenew; }
    public void setAutoRenew(boolean autoRenew) { this.autoRenew = autoRenew; }

    public boolean isFlyActive() { return flyActive; }
    public void setFlyActive(boolean flyActive) { this.flyActive = flyActive; }

    public String getParticleEffect() { return particleEffect; }
    public void setParticleEffect(String particleEffect) { this.particleEffect = particleEffect; }

    public String getOpenSound() { return openSound; }
    public void setOpenSound(String openSound) { this.openSound = openSound; }

    public String getCloseSound() { return closeSound; }
    public void setCloseSound(String closeSound) { this.closeSound = closeSound; }

    public boolean isAfk() { return afk; }
    public void setAfk(boolean afk) { this.afk = afk; }

    public boolean isInCombat() { return inCombat; }
    public void setInCombat(boolean inCombat) { this.inCombat = inCombat; }

    public long getCombatEndTime() { return combatEndTime; }
    public void setCombatEndTime(long combatEndTime) { this.combatEndTime = combatEndTime; }

    public long getLastMovement() { return lastMovement; }
    public void setLastMovement(long lastMovement) { this.lastMovement = lastMovement; }

    public long getFlyStartTime() { return flyStartTime; }
    public void setFlyStartTime(long flyStartTime) { this.flyStartTime = flyStartTime; }

    public boolean hasTime() { return remainingSeconds > 0; }

    public String getFormattedRemaining() {
        long seconds = remainingSeconds;
        if (seconds >= 3600) {
            long hours = seconds / 3600;
            long mins = (seconds % 3600) / 60;
            long secs = seconds % 60;
            return String.format("%ds %02dd %02ds", hours, mins, secs);
        } else if (seconds >= 60) {
            long mins = seconds / 60;
            long secs = seconds % 60;
            return String.format("%dd %02ds", mins, secs);
        } else {
            return seconds + "s";
        }
    }

    public String getFormattedTotalFlown() {
        long seconds = totalSecondsFlown;
        if (seconds >= 3600) {
            long hours = seconds / 3600;
            long mins = (seconds % 3600) / 60;
            return String.format("%ds %02dd", hours, mins);
        } else if (seconds >= 60) {
            long mins = seconds / 60;
            long secs = seconds % 60;
            return String.format("%dd %02ds", mins, secs);
        } else {
            return seconds + "s";
        }
    }
}
