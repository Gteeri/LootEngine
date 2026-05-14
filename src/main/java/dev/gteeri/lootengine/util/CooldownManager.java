package dev.gteeri.lootengine.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<String, Long> cooldowns = new HashMap<>();
    private final long cooldownMillis;

    public CooldownManager(int seconds) {
        this.cooldownMillis = seconds * 1000L;
    }

    public boolean isOnCooldown(UUID playerId, String mobType) {
        if (cooldownMillis <= 0) return false;
        Long last = cooldowns.get(key(playerId, mobType));
        return last != null && System.currentTimeMillis() - last < cooldownMillis;
    }

    public void setCooldown(UUID playerId, String mobType) {
        if (cooldownMillis <= 0) return;
        cooldowns.put(key(playerId, mobType), System.currentTimeMillis());
    }

    public int getRemainingSeconds(UUID playerId, String mobType) {
        Long last = cooldowns.get(key(playerId, mobType));
        if (last == null) return 0;
        long remaining = cooldownMillis - (System.currentTimeMillis() - last);
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }

    public void clearAll() {
        cooldowns.clear();
    }

    private String key(UUID playerId, String mobType) {
        return playerId + ":" + mobType;
    }
}
