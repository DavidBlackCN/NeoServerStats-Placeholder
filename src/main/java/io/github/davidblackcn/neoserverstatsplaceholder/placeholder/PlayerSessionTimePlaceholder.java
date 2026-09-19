package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.DurationFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.player.PlayerSessionTracker;

import net.minecraft.server.level.ServerPlayer;

/**
 * {@code %player_session_time%} — time elapsed since this player's current login, for example
 * {@code 2h 17m 33s}.
 *
 * <p>Deliberately not the vanilla cumulative statistic: this is the current session only, tracked
 * in memory by {@link PlayerSessionTracker} and lost on logout or restart.
 */
final class PlayerSessionTimePlaceholder extends PlayerPlaceholder {

    private final PlayerSessionTracker sessionTracker;

    PlayerSessionTimePlaceholder(PlayerSessionTracker sessionTracker) {
        super("session_time", 0,
                List.of("Time since this player logged in, for example 2h 17m 33s"),
                List.of("%player_session_time%"));

        this.sessionTracker = sessionTracker;
    }

    @Override
    protected String resolve(ServerPlayer player) {
        long sessionSeconds = this.sessionTracker.sessionSeconds(player.getUUID());

        if (sessionSeconds < 0L) {
            return PlaceholderFallback.NOT_AVAILABLE;
        }

        return DurationFormatter.formatSession(sessionSeconds);
    }
}
