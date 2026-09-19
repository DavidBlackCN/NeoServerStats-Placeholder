package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;

/** {@code %player_uuid%} — the player's UUID in canonical dashed form. */
final class PlayerUuidPlaceholder extends PlayerPlaceholder {

    PlayerUuidPlaceholder() {
        super("uuid", 0,
                List.of("Player UUID in canonical form, for example 069a79f4-44e9-4726-a5be-fca90e38aaf5"),
                List.of("%player_uuid%"));
    }

    @Override
    protected String resolve(ServerPlayer player) {
        // UUID#toString is the canonical dashed representation.
        return player.getUUID().toString();
    }
}
