package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.player.PlayerStatistics;

import net.minecraft.server.level.ServerPlayer;

/**
 * {@code %player_playtime_ticks%} — raw vanilla playtime in game ticks.
 *
 * <p>Exposes the statistic exactly as vanilla stores it, with no rounding.
 */
final class PlayerPlaytimeTicksPlaceholder extends PlayerPlaceholder {

    PlayerPlaytimeTicksPlaceholder() {
        super("playtime_ticks", 0,
                List.of("Cumulative playtime in raw vanilla ticks, for example 7812345"),
                List.of("%player_playtime_ticks%"));
    }

    @Override
    protected String resolve(ServerPlayer player) {
        return Integer.toString(PlayerStatistics.playtimeTicks(player));
    }
}
