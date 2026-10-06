package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

/** Literal public keys shared by every consumer. */
public final class PlaceholderCatalog {
    public static final List<String> SERVER_KEYS = List.of(
            "version", "online", "max_players", "players_list", "uptime", "motd",
            "memory_used", "memory_committed", "memory_max", "memory_percent",
            "tps", "mspt", "cpu_process", "cpu_system", "name", "variant", "build", "version_full",
            "has_whitelist", "ram_used", "ram_free", "ram_total", "ram_max", "tps_current",
            "tps_1", "tps_5", "tps_15", "tps_1_colored", "tps_5_colored", "tps_15_colored");
    public static final List<String> PLAYER_KEYS = List.of(
            "name", "uuid", "ping", "dimension", "x", "y", "z", "deaths", "playtime",
            "playtime_ticks", "playtime_seconds", "playtime_hours", "session_time",
            "displayname", "list_name", "world", "world_type", "x_long", "y_long", "z_long",
            "yaw", "pitch", "biome", "biome_capitalized", "health", "health_rounded", "max_health",
            "max_health_rounded", "food_level", "saturation", "gamemode", "online", "allow_flight",
            "is_flying", "is_sneaking", "is_sprinting", "is_sleeping", "is_inside_vehicle",
            "level", "exp", "current_exp", "total_exp", "exp_to_level", "colored_ping");
    private PlaceholderCatalog() {}
    public static List<String> keys(String namespace) {
        return switch (namespace) {
            case "server" -> SERVER_KEYS;
            case "player" -> PLAYER_KEYS;
            default -> List.of();
        };
    }
}
