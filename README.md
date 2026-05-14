# 🎲 LootEngine

Advanced custom loot drop system for Paper 1.21+ Minecraft servers.

## Features

- **Custom mob drops** — Configure unique items to drop from any mob
- **Rarity system** — Common, Uncommon, Rare, Epic, Legendary tiers
- **Custom items** — Display names, lore, enchantments
- **Visual effects** — Particles and sounds on rare drops
- **Legendary broadcasts** — Server-wide announcements for legendary drops
- **GUI preview** — In-game inventory GUI to browse loot tables
- **Hot reload** — Reload config without restarting the server
- **Tab completion** — Full tab-complete support for all commands

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/loot reload` | Reload configuration | `lootengine.admin` |
| `/loot preview <mob>` | Open loot table GUI | `lootengine.admin` |
| `/loot give <mob>` | Roll and receive loot | `lootengine.admin` |

## Configuration

All loot tables are defined in `loot.yml`:

```yaml
tables:
  zombie:
    - item: DIAMOND
      chance: 5.0
      amount: 1
      rarity: LEGENDARY
      display-name: "&b&lZombie's Diamond"
      lore:
        - "&7Dropped from a zombie"
        - "&7with a 5% chance"
      enchantments:
        SHARPNESS: 3
      effects:
        particles: true
        sound: ENTITY_PLAYER_LEVELUP
```

### Entry options

| Field | Type | Description |
|-------|------|-------------|
| `item` | Material | Bukkit material name |
| `chance` | Double | Drop chance (0-100%) |
| `amount` | Int or Range | Amount like `1` or `1-3` |
| `rarity` | Rarity | COMMON, UNCOMMON, RARE, EPIC, LEGENDARY |
| `display-name` | String | Custom item name (supports `&` color codes) |
| `lore` | List | Item lore lines |
| `enchantments` | Map | Enchantment name → level |
| `effects.particles` | Boolean | Show particles on drop |
| `effects.sound` | String | Bukkit sound name to play |

## Installation

1. Download the latest release from [Releases](../../releases)
2. Place the `.jar` in your server's `plugins/` folder
3. Start/restart the server
4. Edit `plugins/LootEngine/loot.yml` to configure drops
5. Use `/loot reload` to apply changes

## Requirements

- Paper 1.21+ (or any Paper fork like Purpur)
- Java 21+

## Building

```bash
mvn clean package
```

The compiled jar will be in `target/LootEngine-1.0.0.jar`

## License

MIT License — feel free to use, modify and distribute.
