package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.DurationFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.player.PlayerStatistics;

import net.minecraft.server.level.ServerPlayer;

/**
 * {@code %player_playtime_hours%} — cumulative vanilla playtime in hours with two decimals.
 *
 * <p>Same vanilla source as the other playtime placeholders, only rescaled.
 */
final class PlayerPlaytimeHoursPlaceholder extends PlayerPlaceholder {

    PlayerPlaytimeHoursPlaceholder() {
        super("playtime_hours", 0,
                List.of("Cumulative playtime in hours with two decimals, for example 108.58"),
                List.of("%player_playtime_hours%"));
    }

    @Override
    protected String resolve(ServerPlayer player) {
        double hours = DurationFormatter.hoursFromTicks(PlayerStatistics.playtimeTicks(player));
        return DurationFormatter.formatHours(hours);
    }
}
