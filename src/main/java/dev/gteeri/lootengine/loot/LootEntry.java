package dev.gteeri.lootengine.loot;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LootEntry {

    private final Material material;
    private final double chance;
    private final int minAmount;
    private final int maxAmount;
    private final Rarity rarity;
    private final String displayName;
    private final List<String> lore;
    private final Map<Enchantment, Integer> enchantments;
    private final boolean hasParticles;
    private final String sound;
    private final LootCondition condition;

    public LootEntry(Material material, double chance, int minAmount, int maxAmount,
                     Rarity rarity, String displayName, List<String> lore,
                     Map<Enchantment, Integer> enchantments, boolean hasParticles,
                     String sound, LootCondition condition) {
        this.material = material;
        this.chance = chance;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.rarity = rarity;
        this.displayName = displayName;
        this.lore = lore;
        this.enchantments = enchantments;
        this.hasParticles = hasParticles;
        this.sound = sound;
        this.condition = condition;
    }

    public boolean rollChance() {
        return ThreadLocalRandom.current().nextDouble(100.0) < chance;
    }

    public ItemStack createItem() {
        int amount = minAmount == maxAmount
                ? minAmount
                : ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1);

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (displayName != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            }

            if (lore != null && !lore.isEmpty()) {
                List<String> colored = new ArrayList<>();
                for (String line : lore) {
                    colored.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(colored);
            }

            item.setItemMeta(meta);
        }

        if (enchantments != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }
        }

        return item;
    }

    public Material getMaterial() { return material; }
    public double getChance() { return chance; }
    public Rarity getRarity() { return rarity; }
    public boolean hasParticles() { return hasParticles; }
    public String getSound() { return sound; }
    public String getDisplayName() { return displayName; }
    public LootCondition getCondition() { return condition; }

    @SuppressWarnings("unchecked")
    public static LootEntry fromMap(Map<?, ?> map) {
        try {
            Material material = Material.valueOf(((String) map.get("item")).toUpperCase());
            double chance = ((Number) map.get("chance")).doubleValue();

            int minAmount = 1, maxAmount = 1;
            Object amountObj = map.get("amount");
            if (amountObj instanceof Number num) {
                minAmount = num.intValue();
                maxAmount = minAmount;
            } else if (amountObj instanceof String str) {
                String[] parts = str.split("-");
                minAmount = Integer.parseInt(parts[0]);
                maxAmount = parts.length > 1 ? Integer.parseInt(parts[1]) : minAmount;
            }

            Rarity rarity = Rarity.COMMON;
            if (map.containsKey("rarity")) {
                try { rarity = Rarity.valueOf(((String) map.get("rarity")).toUpperCase()); }
                catch (IllegalArgumentException ignored) {}
            }

            String displayName = (String) map.get("display-name");
            List<String> lore = (List<String>) map.get("lore");

            Map<Enchantment, Integer> enchantments = new HashMap<>();
            if (map.containsKey("enchantments")) {
                Map<String, Object> enchMap = (Map<String, Object>) map.get("enchantments");
                for (Map.Entry<String, Object> entry : enchMap.entrySet()) {
                    Enchantment ench = Enchantment.getByName(entry.getKey().toUpperCase());
                    if (ench != null) {
                        enchantments.put(ench, ((Number) entry.getValue()).intValue());
                    }
                }
            }

            boolean hasParticles = false;
            String sound = null;
            if (map.containsKey("effects")) {
                Map<String, Object> effects = (Map<String, Object>) map.get("effects");
                hasParticles = Boolean.TRUE.equals(effects.get("particles"));
                sound = (String) effects.get("sound");
            }

            LootCondition condition = LootCondition.fromMap(map);

            return new LootEntry(material, chance, minAmount, maxAmount, rarity,
                    displayName, lore, enchantments, hasParticles, sound, condition);
        } catch (Exception e) {
            return null;
        }
    }
}
