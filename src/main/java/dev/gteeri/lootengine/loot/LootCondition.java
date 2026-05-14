package dev.gteeri.lootengine.loot;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;

public class LootCondition {

    private final TimeOfDay timeOfDay;
    private final Weather weather;
    private final String permission;
    private final Integer minPlayerLevel;

    public LootCondition(TimeOfDay timeOfDay, Weather weather, String permission, Integer minPlayerLevel) {
        this.timeOfDay = timeOfDay;
        this.weather = weather;
        this.permission = permission;
        this.minPlayerLevel = minPlayerLevel;
    }

    public boolean check(Player player, World world) {
        if (timeOfDay != null && !checkTime(world)) return false;
        if (weather != null && !checkWeather(world)) return false;
        if (permission != null && !player.hasPermission(permission)) return false;
        if (minPlayerLevel != null && player.getLevel() < minPlayerLevel) return false;
        return true;
    }

    private boolean checkTime(World world) {
        long time = world.getTime();
        return switch (timeOfDay) {
            case DAY -> time >= 0 && time < 12300;
            case NIGHT -> time >= 12300 && time < 24000;
            case ANY -> true;
        };
    }

    private boolean checkWeather(World world) {
        return switch (weather) {
            case CLEAR -> !world.hasStorm();
            case RAIN -> world.hasStorm() && !world.isThundering();
            case THUNDER -> world.isThundering();
            case ANY -> true;
        };
    }

    @SuppressWarnings("unchecked")
    public static LootCondition fromMap(Map<?, ?> map) {
        if (!map.containsKey("conditions")) return null;

        Map<?, ?> condMap = (Map<?, ?>) map.get("conditions");
        if (condMap == null) return null;

        TimeOfDay time = TimeOfDay.ANY;
        if (condMap.containsKey("time")) {
            try { time = TimeOfDay.valueOf(((String) condMap.get("time")).toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        Weather weather = Weather.ANY;
        if (condMap.containsKey("weather")) {
            try { weather = Weather.valueOf(((String) condMap.get("weather")).toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        String permission = (String) condMap.get("permission");
        Integer minLevel = condMap.containsKey("min-level")
                ? ((Number) condMap.get("min-level")).intValue() : null;

        if (time == TimeOfDay.ANY && weather == Weather.ANY && permission == null && minLevel == null) {
            return null;
        }

        return new LootCondition(time, weather, permission, minLevel);
    }

    public enum TimeOfDay { DAY, NIGHT, ANY }
    public enum Weather { CLEAR, RAIN, THUNDER, ANY }
}
