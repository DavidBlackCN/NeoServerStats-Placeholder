package io.github.davidblackcn.neoserverstatsplaceholder.player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import io.github.davidblackcn.neoserverstatsplaceholder.format.CompatibilityFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.format.DurationFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderFallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/** Captures player state only on the server thread. No offline loading or statistic writes. */
public final class PlayerSnapshotCollector {
    private PlayerSnapshotCollector() {}
    public static Map<String, String> capture(ServerPlayer player, long sessionSeconds) {
        Map<String, String> values = new HashMap<>();
        String name = player.getGameProfile().getName();
        values.put("name", name);
        values.put("uuid", player.getUUID().toString());
        values.put("displayname", player.getDisplayName().getString());
        var tabName = player.getTabListDisplayName();
        values.put("list_name", tabName == null ? name : tabName.getString());
        int ping = player.connection.latency();
        values.put("ping", Integer.toString(ping));
        values.put("colored_ping", CompatibilityFormatter.coloredPing(ping));
        var dimension = player.serverLevel().dimension();
        values.put("dimension", dimension.location().toString());
        String worldName = player.server.getWorldData().getLevelName();
        values.put("world", dimension.equals(Level.OVERWORLD) ? worldName
                : dimension.equals(Level.NETHER) ? worldName + "_nether"
                : dimension.equals(Level.END) ? worldName + "_the_end" : dimension.location().toString());
        values.put("world_type", dimension.equals(Level.OVERWORLD) ? "Overworld"
                : dimension.equals(Level.NETHER) ? "Nether"
                : dimension.equals(Level.END) ? "The End" : PlaceholderFallback.NOT_AVAILABLE);
        coordinate(values, "x", player.getX());
        coordinate(values, "y", player.getY());
        coordinate(values, "z", player.getZ());
        values.put("yaw", Float.toString(player.getYRot()));
        values.put("pitch", Float.toString(player.getXRot()));
        String biome = player.serverLevel().getBiome(player.blockPosition()).unwrapKey()
                .map(key -> biomeName(key.location())).orElse(PlaceholderFallback.NOT_AVAILABLE);
        values.put("biome", biome);
        values.put("biome_capitalized", CompatibilityFormatter.capitalizedBiome(biome));
        values.put("health", Double.toString(player.getHealth()));
        values.put("health_rounded", Long.toString(Math.round((double) player.getHealth())));
        values.put("max_health", Double.toString(player.getMaxHealth()));
        values.put("max_health_rounded", Long.toString(Math.round((double) player.getMaxHealth())));
        values.put("food_level", Integer.toString(player.getFoodData().getFoodLevel()));
        values.put("saturation", Float.toString(player.getFoodData().getSaturationLevel()));
        values.put("gamemode", player.gameMode.getGameModeForPlayer().getName().toUpperCase(Locale.ROOT));
        values.put("online", CompatibilityFormatter.bool(true));
        values.put("allow_flight", CompatibilityFormatter.bool(player.getAbilities().mayfly));
        values.put("is_flying", CompatibilityFormatter.bool(player.getAbilities().flying));
        values.put("is_sneaking", CompatibilityFormatter.bool(player.isShiftKeyDown()));
        values.put("is_sprinting", CompatibilityFormatter.bool(player.isSprinting()));
        values.put("is_sleeping", CompatibilityFormatter.bool(player.isSleeping()));
        values.put("is_inside_vehicle", CompatibilityFormatter.bool(player.isPassenger()));
        values.put("level", Integer.toString(player.experienceLevel));
        values.put("exp", Float.toString(player.experienceProgress));
        values.put("current_exp", Long.toString(ExperienceCalculator.currentExperience(
                player.experienceLevel, player.experienceProgress, player.getXpNeededForNextLevel())));
        values.put("total_exp", Integer.toString(player.totalExperience));
        values.put("exp_to_level", Integer.toString(player.getXpNeededForNextLevel()));
        values.put("deaths", Integer.toString(PlayerStatistics.deaths(player)));
        int playtime = PlayerStatistics.playtimeTicks(player);
        values.put("playtime_ticks", Integer.toString(playtime));
        values.put("playtime_seconds", Long.toString(DurationFormatter.secondsFromTicks(playtime)));
        values.put("playtime_hours", DurationFormatter.formatHours(DurationFormatter.hoursFromTicks(playtime)));
        values.put("playtime", DurationFormatter.formatUptime(DurationFormatter.secondsFromTicks(playtime)));
        values.put("session_time", DurationFormatter.formatSession(sessionSeconds));
        return Map.copyOf(values);
    }
    private static void coordinate(Map<String, String> values, String axis, double value) {
        values.put(axis, CompatibilityFormatter.blockCoordinate(value));
        values.put(axis + "_long", Double.toString(value));
    }
    private static String biomeName(ResourceLocation id) {
        return id.getNamespace().equals("minecraft") ? id.getPath().toUpperCase(Locale.ROOT) : id.toString();
    }
}
