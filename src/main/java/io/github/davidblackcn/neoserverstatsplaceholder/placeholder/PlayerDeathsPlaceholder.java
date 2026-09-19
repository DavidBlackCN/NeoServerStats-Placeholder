package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.player.PlayerStatistics;

import net.minecraft.server.level.ServerPlayer;

/**
 * {@code %player_deaths%} — the vanilla death counter.
 *
 * <p>Read from the player's vanilla statistics rather than a counter of our own, so it always
 * matches what the vanilla statistics screen shows.
 */
final class PlayerDeathsPlaceholder extends PlayerPlaceholder {

    PlayerDeathsPlaceholder() {
        super("deaths", 0,
                List.of("Number of deaths, from the vanilla statistics"),
                List.of("%player_deaths%"));
    }

    @Override
    protected String resolve(ServerPlayer player) {
        return Integer.toString(PlayerStatistics.deaths(player));
    }
}
