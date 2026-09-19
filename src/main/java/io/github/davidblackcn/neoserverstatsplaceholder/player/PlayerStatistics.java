package io.github.davidblackcn.neoserverstatsplaceholder.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;

/**
 * Reads the values vanilla Minecraft already tracks for a player.
 *
 * <p>Deaths and cumulative playtime come straight from the player's own vanilla statistics
 * ({@code minecraft:deaths} and {@code minecraft:play_time}). Nothing here writes, resets or
 * duplicates that data, and no side file, database or counter is created: the goal is to expose the
 * values that already exist.
 *
 * <p>This is also the single place that documents the unit of the playtime statistic.
 */
public final class PlayerStatistics {

    private PlayerStatistics() {}

    /** The vanilla {@code minecraft:deaths} custom statistic, as a whole count. */
    public static int deaths(ServerPlayer player) {
        return player.getStats().getValue(Stats.CUSTOM, Stats.DEATHS);
    }

    /**
     * The vanilla {@code minecraft:play_time} custom statistic.
     *
     * <p>The unit is <strong>game ticks</strong>: vanilla registers this statistic with
     * {@code StatFormatter.TIME}, which divides the raw value by 20 to get seconds, and
     * {@code Player#tick()} awards it once per tick. Use
     * {@link io.github.davidblackcn.neoserverstatsplaceholder.format.DurationFormatter} to convert.
     */
    public static int playtimeTicks(ServerPlayer player) {
        return player.getStats().getValue(Stats.CUSTOM, Stats.PLAY_TIME);
    }
}
