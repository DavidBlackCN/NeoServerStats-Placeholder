package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.CoordinateFormatter;

import net.minecraft.server.level.ServerPlayer;

/** {@code %player_x%} — the player's X coordinate, always with two decimals. */
final class PlayerXPlaceholder extends PlayerPlaceholder {

    PlayerXPlaceholder() {
        super("x", 0,
                List.of("Player X coordinate with two decimals, for example -128.50"),
                List.of("%player_x%"));
    }

    @Override
    protected String resolve(ServerPlayer player) {
        return CoordinateFormatter.format(player.getX());
    }
}
