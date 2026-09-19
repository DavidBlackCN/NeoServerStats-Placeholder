package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;

/** {@code %player_name%} — the player's exact account name. */
final class PlayerNamePlaceholder extends PlayerPlaceholder {

    PlayerNamePlaceholder() {
        super("name", 0,
                List.of("Exact current player name"),
                List.of("%player_name%"));
    }

    @Override
    protected String resolve(ServerPlayer player) {
        // The game profile name is the canonical account name, with no display formatting.
        return player.getGameProfile().getName();
    }
}
