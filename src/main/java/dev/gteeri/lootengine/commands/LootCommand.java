package dev.gteeri.lootengine.commands;

import dev.gteeri.lootengine.LootEngine;
import dev.gteeri.lootengine.gui.LootPreviewGUI;
import dev.gteeri.lootengine.lang.MessageManager;
import dev.gteeri.lootengine.loot.LootEntry;
import dev.gteeri.lootengine.stats.PlayerStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LootCommand implements CommandExecutor, TabCompleter {

    private final LootEngine plugin;

    public LootCommand(LootEngine plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MessageManager msg = plugin.getMessageManager();

        if (!sender.hasPermission("lootengine.admin")) {
            sender.sendMessage(msg.getPrefixed("commands.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, msg);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(msg.getPrefixed("commands.reload-success"));
            }
            case "preview" -> handlePreview(sender, args, msg);
            case "give" -> handleGive(sender, args, msg);
            case "stats" -> handleStats(sender, msg);
            default -> sendHelp(sender, msg);
        }

        return true;
    }

    private void handlePreview(CommandSender sender, String[] args, MessageManager msg) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(msg.getPrefixed("commands.player-only"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(msg.getPrefixed("commands.help-preview"));
            return;
        }

        EntityType type = parseEntityType(args[1]);
        if (type == null) {
            player.sendMessage(msg.getPrefixed("commands.unknown-mob", MessageManager.of("mob", args[1])));
            return;
        }

        List<LootEntry> entries = plugin.getLootManager().getLootFor(type);
        if (entries.isEmpty()) {
            player.sendMessage(msg.getPrefixed("commands.no-loot-configured",
                    MessageManager.of("mob", args[1].toLowerCase())));
            return;
        }

        LootPreviewGUI.open(player, type, entries, msg);
    }

    private void handleGive(CommandSender sender, String[] args, MessageManager msg) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(msg.getPrefixed("commands.player-only"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(msg.getPrefixed("commands.help-give"));
            return;
        }

        EntityType type = parseEntityType(args[1]);
        if (type == null) {
            player.sendMessage(msg.getPrefixed("commands.unknown-mob", MessageManager.of("mob", args[1])));
            return;
        }

        List<LootEntry> entries = plugin.getLootManager().getLootFor(type);
        if (entries.isEmpty()) {
            player.sendMessage(msg.getPrefixed("commands.no-loot-configured",
                    MessageManager.of("mob", args[1].toLowerCase())));
            return;
        }

        int dropped = 0;
        for (LootEntry entry : entries) {
            if (entry.rollChance()) {
                player.getInventory().addItem(entry.createItem());
                dropped++;
            }
        }

        player.sendMessage(msg.getPrefixed("commands.give-result",
                MessageManager.of("total", String.valueOf(entries.size()), "dropped", String.valueOf(dropped))));
    }

    private void handleStats(CommandSender sender, MessageManager msg) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(msg.getPrefixed("commands.player-only"));
            return;
        }

        PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId());
        player.sendMessage(msg.get("stats.header"));
        player.sendMessage(msg.get("stats.total-kills", MessageManager.of("kills", String.valueOf(stats.getTotalKills()))));
        player.sendMessage(msg.get("stats.total-drops", MessageManager.of("drops", String.valueOf(stats.getTotalDrops()))));
        player.sendMessage(msg.get("stats.legendaries", MessageManager.of("count", String.valueOf(stats.getLegendaryDrops()))));
        player.sendMessage(msg.get("stats.favorite-mob", MessageManager.of(
                "mob", stats.getFavoriteMob(), "kills", String.valueOf(stats.getKillsForMob(stats.getFavoriteMob())))));
    }

    private void sendHelp(CommandSender sender, MessageManager msg) {
        sender.sendMessage(msg.get("commands.help-header"));
        sender.sendMessage(msg.get("commands.help-reload"));
        sender.sendMessage(msg.get("commands.help-preview"));
        sender.sendMessage(msg.get("commands.help-give"));
        sender.sendMessage(msg.get("commands.help-stats"));
    }

    private EntityType parseEntityType(String name) {
        try { return EntityType.valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("reload", "preview", "give", "stats"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("preview") || args[0].equalsIgnoreCase("give"))) {
            for (EntityType type : plugin.getLootManager().getConfiguredMobs()) {
                completions.add(type.name().toLowerCase());
            }
        }

        String input = args[args.length - 1].toLowerCase();
        return completions.stream().filter(s -> s.startsWith(input)).toList();
    }
}
