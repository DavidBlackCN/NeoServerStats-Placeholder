package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.DurationFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.player.PlayerStatistics;

import net.minecraft.server.level.ServerPlayer;

/**
 * {@code %player_playtime%} — cumulative vanilla playtime, human readable.
 *
 * <p>Cumulative means the whole lifetime of the player on this server, from the vanilla statistics,
 * not the current session ({@code %player_session_time%} covers that).
 */
final class PlayerPlaytimePlaceholder extends PlayerPlaceholder {

    PlayerPlaytimePlaceholder() {
        super("playtime", 0,
                List.of("Cumulative playtime from the vanilla statistics, for example 4d 12h 35m"),
                List.of("%player_playtime%"));
    }

    @Override
    protected String resolve(ServerPlayer player) {
        long seconds = DurationFormatter.secondsFromTicks(PlayerStatistics.playtimeTicks(player));
        return DurationFormatter.formatUptime(seconds);
    }
}
