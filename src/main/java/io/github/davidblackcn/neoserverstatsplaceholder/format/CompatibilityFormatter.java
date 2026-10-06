package io.github.davidblackcn.neoserverstatsplaceholder.format;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderFallback;

/** eCloud default formatting, implemented without Bukkit dependencies. */
public final class CompatibilityFormatter {
    private CompatibilityFormatter() {}
    public static String bool(boolean value) { return value ? "yes" : "no"; }
    public static String blockCoordinate(double coordinate) { return Long.toString((long) Math.floor(coordinate)); }
    public static String coloredPing(int ping) { return (ping > 100 ? "\u00a7c" : ping > 50 ? "\u00a7e" : "\u00a7a") + ping; }
    public static String tps(double value) { return Double.toString(Math.min(20.0, Math.round(Math.max(0, value)))); }
    public static String coloredTps(double value) { return (value > 18 ? "\u00a7a" : value > 16 ? "\u00a7e" : "\u00a7c") + tps(value); }
    public static String uptime(long totalSeconds) {
        long remaining = Math.max(0, totalSeconds);
        StringBuilder result = new StringBuilder();
        long[] units = {604800, 86400, 3600, 60, 1};
        String[] suffixes = {"w", "d", "h", "m", "s"};
        for (int i = 0; i < units.length; i++) {
            long value = remaining / units[i];
            remaining %= units[i];
            if (value > 0) {
                if (!result.isEmpty()) result.append(' ');
                result.append(value).append(suffixes[i]);
            }
        }
        return result.isEmpty() ? "0s" : result.toString();
    }
    public static String capitalizedBiome(String biome) {
        if (biome.equals(PlaceholderFallback.NOT_AVAILABLE) || biome.contains(":")) return biome;
        return Arrays.stream(biome.toLowerCase(Locale.ROOT).split("_"))
                .map(word -> word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
