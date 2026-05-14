package dev.gteeri.lootengine.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player cooldowns to prevent loot farming exploits.
 * Configurable cooldown period between drops from the same mob type.
 */
public class CooldownManager {

    private final Map<String, Long> cooldowns = new HashMap<>();
    private final long cooldownMillis;

    /**
     * Creates a cooldown manager with the specified cooldown duration.
     *
     * @param cooldownSeconds cooldown duration in seconds
     */
    public CooldownManager(int cooldownSeconds) {
        this.cooldownMillis = cooldownSeconds * 1000L;
    }

    /**
     * Checks if a player is on cooldown for a specific mob type.
     *
     * @param playerId the player's UUID
     * @param mobType  the mob type
     * @return true if the player is still on cooldown
     */
    public boolean isOnCooldown(UUID playerId, String mobType) {
        if (cooldownMillis <= 0) return false;

        String key = playerId.toString() + ":" + mobType;
        Long lastDrop = cooldowns.get(key);

        if (lastDrop == null) return false;
        return System.currentTimeMillis() - lastDrop < cooldownMillis;
    }

    /**
     * Sets the cooldown for a player and mob type.
     *
     * @param playerId the player's UUID
     * @param mobType  the mob type
     */
    public void setCooldown(UUID playerId, String mobType) {
        if (cooldownMillis <= 0) return;
        String key = playerId.toString() + ":" + mobType;
        cooldowns.put(key, System.currentTimeMillis());
    }

    /**
     * Gets remaining cooldown time in seconds.
     *
     * @param playerId the player's UUID
     * @param mobType  the mob type
     * @return remaining seconds, or 0 if not on cooldown
     */
    public int getRemainingSeconds(UUID playerId, String mobType) {
        String key = playerId.toString() + ":" + mobType;
        Long lastDrop = cooldowns.get(key);

        if (lastDrop == null) return 0;

        long elapsed = System.currentTimeMillis() - lastDrop;
        long remaining = cooldownMillis - elapsed;

        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }

    /**
     * Clears all cooldowns. Used on plugin reload.
     */
    public void clearAll() {
        cooldowns.clear();
    }
}
