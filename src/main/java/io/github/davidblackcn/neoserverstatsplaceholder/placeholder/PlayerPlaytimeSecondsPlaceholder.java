package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.DurationFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.player.PlayerStatistics;

import net.minecraft.server.level.ServerPlayer;

/**
 * {@code %player_playtime_seconds%} — cumulative vanilla playtime in whole seconds.
 *
 * <p>Derived from the vanilla tick counter at the documented ratio of 20 ticks per second, so the
 * value is an integer without a decimal separator.
 */
final class PlayerPlaytimeSecondsPlaceholder extends PlayerPlaceholder {

    PlayerPlaytimeSecondsPlaceholder() {
        super("playtime_seconds", 0,
                List.of("Cumulative playtime in whole seconds, for example 390617"),
                List.of("%player_playtime_seconds%"));
    }

    @Override
    protected String resolve(ServerPlayer player) {
        return Long.toString(DurationFormatter.secondsFromTicks(PlayerStatistics.playtimeTicks(player)));
    }
}
