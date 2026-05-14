package dev.gteeri.lootengine.commands;

import dev.gteeri.lootengine.LootEngine;
import dev.gteeri.lootengine.gui.LootPreviewGUI;
import dev.gteeri.lootengine.loot.LootEntry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the /loot command with subcommands: reload, preview, give.
 */
public class LootCommand implements CommandExecutor, TabCompleter {

    private final LootEngine plugin;

    public LootCommand(LootEngine plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "preview" -> handlePreview(sender, args);
            case "give" -> handleGive(sender, args);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "[LootEngine] Configuration reloaded successfully.");
    }

    private void handlePreview(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /loot preview <mob>");
            return;
        }

        String mobName = args[1].toUpperCase();
        EntityType type;
        try {
            type = EntityType.valueOf(mobName);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown mob type: " + args[1]);
            return;
        }

        List<LootEntry> entries = plugin.getLootManager().getLootFor(type);
        if (entries.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No loot configured for " + mobName.toLowerCase());
            return;
        }

        LootPreviewGUI.open(player, type, entries);
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /loot give <mob>");
            return;
        }

        String mobName = args[1].toUpperCase();
        EntityType type;
        try {
            type = EntityType.valueOf(mobName);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown mob type: " + args[1]);
            return;
        }

        List<LootEntry> entries = plugin.getLootManager().getLootFor(type);
        if (entries.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No loot configured for " + mobName.toLowerCase());
            return;
        }

        // Roll all entries and give drops to player
        int dropped = 0;
        for (LootEntry entry : entries) {
            if (entry.rollChance()) {
                player.getInventory().addItem(entry.createItem());
                dropped++;
            }
        }

        player.sendMessage(ChatColor.GREEN + "[LootEngine] Rolled " + entries.size()
                + " entries, got " + dropped + " drops!");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== LootEngine Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/loot reload " + ChatColor.GRAY + "- Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/loot preview <mob> " + ChatColor.GRAY + "- Preview loot table");
        sender.sendMessage(ChatColor.YELLOW + "/loot give <mob> " + ChatColor.GRAY + "- Roll and give loot");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
            completions.add("preview");
            completions.add("give");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("preview") || args[0].equalsIgnoreCase("give"))) {
            for (EntityType type : plugin.getLootManager().getConfiguredMobs()) {
                completions.add(type.name().toLowerCase());
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
}
