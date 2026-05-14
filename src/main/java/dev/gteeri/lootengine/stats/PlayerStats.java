package dev.gteeri.lootengine.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStats {

    private final UUID playerId;
    private int totalKills;
    private int totalDrops;
    private int legendaryDrops;
    private final Map<String, Integer> killsByMob;

    public PlayerStats(UUID playerId) {
        this.playerId = playerId;
        this.killsByMob = new HashMap<>();
    }

    public void addKill(String mobType) {
        totalKills++;
        killsByMob.merge(mobType, 1, Integer::sum);
    }

    public void addDrop(boolean legendary) {
        totalDrops++;
        if (legendary) legendaryDrops++;
    }

    public UUID getPlayerId() { return playerId; }
    public int getTotalKills() { return totalKills; }
    public int getTotalDrops() { return totalDrops; }
    public int getLegendaryDrops() { return legendaryDrops; }

    public String getFavoriteMob() {
        return killsByMob.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
    }

    public int getKillsForMob(String mob) {
        return killsByMob.getOrDefault(mob, 0);
    }

    public Map<String, Integer> getKillsByMob() {
        return Map.copyOf(killsByMob);
    }
}
