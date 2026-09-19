package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;

/** {@code %player_dimension%} — the dimension the player is currently in. */
final class PlayerDimensionPlaceholder extends PlayerPlaceholder {

    PlayerDimensionPlaceholder() {
        super("dimension", 0,
                List.of("Dimension the player is in, for example minecraft:overworld"),
                List.of("%player_dimension%"));
    }

    @Override
    protected String resolve(ServerPlayer player) {
        // The resource location form is stable and unambiguous, unlike the dimension's display name.
        return player.level().dimension().location().toString();
    }
}
