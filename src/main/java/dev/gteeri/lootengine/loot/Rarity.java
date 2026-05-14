package dev.gteeri.lootengine.loot;

import org.bukkit.ChatColor;

/**
 * Represents the rarity tier of a loot drop.
 * Each rarity has an associated color for display.
 */
public enum Rarity {

    COMMON(ChatColor.WHITE, "Common"),
    UNCOMMON(ChatColor.GREEN, "Uncommon"),
    RARE(ChatColor.BLUE, "Rare"),
    EPIC(ChatColor.DARK_PURPLE, "Epic"),
    LEGENDARY(ChatColor.GOLD, "Legendary");

    private final ChatColor color;
    private final String displayName;

    Rarity(ChatColor color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getDisplayName() {
        return color + displayName;
    }
}
