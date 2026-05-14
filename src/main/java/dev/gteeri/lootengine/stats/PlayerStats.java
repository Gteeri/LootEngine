package dev.gteeri.lootengine.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks loot statistics for a single player.
 * Stores kill counts, drop counts, and legendary drop history.
 */
public class PlayerStats {

    private final UUID playerId;
    private int totalKills;
    private int totalDrops;
    private int legendaryDrops;
    private final Map<String, Integer> killsByMob;

    public PlayerStats(UUID playerId) {
        this.playerId = playerId;
        this.totalKills = 0;
        this.totalDrops = 0;
        this.legendaryDrops = 0;
        this.killsByMob = new HashMap<>();
    }

    public void addKill(String mobType) {
        totalKills++;
        killsByMob.merge(mobType, 1, Integer::sum);
    }

    public void addDrop(boolean isLegendary) {
        totalDrops++;
        if (isLegendary) legendaryDrops++;
    }

    public UUID getPlayerId() { return playerId; }
    public int getTotalKills() { return totalKills; }
    public int getTotalDrops() { return totalDrops; }
    public int getLegendaryDrops() { return legendaryDrops; }

    /**
     * Gets the mob type the player has killed the most.
     *
     * @return the most killed mob name, or "None" if no kills
     */
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
