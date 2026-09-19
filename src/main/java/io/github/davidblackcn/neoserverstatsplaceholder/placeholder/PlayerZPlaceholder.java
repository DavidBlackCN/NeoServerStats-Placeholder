package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.CoordinateFormatter;

import net.minecraft.server.level.ServerPlayer;

/** {@code %player_z%} — the player's Z coordinate, always with two decimals. */
final class PlayerZPlaceholder extends PlayerPlaceholder {

    PlayerZPlaceholder() {
        super("z", 0,
                List.of("Player Z coordinate with two decimals, for example 512.25"),
                List.of("%player_z%"));
    }

    @Override
    protected String resolve(ServerPlayer player) {
        return CoordinateFormatter.format(player.getZ());
    }
}
