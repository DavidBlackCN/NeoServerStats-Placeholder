package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.CoordinateFormatter;

import net.minecraft.server.level.ServerPlayer;

/** {@code %player_y%} — the player's Y coordinate, always with two decimals. */
final class PlayerYPlaceholder extends PlayerPlaceholder {

    PlayerYPlaceholder() {
        super("y", 0,
                List.of("Player Y coordinate with two decimals, for example 64.00"),
                List.of("%player_y%"));
    }

    @Override
    protected String resolve(ServerPlayer player) {
        return CoordinateFormatter.format(player.getY());
    }
}
