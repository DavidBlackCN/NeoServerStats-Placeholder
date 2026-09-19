package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import net.minecraft.server.MinecraftServer;

/** {@code %server_max_players%} — configured player limit from {@code server.properties}. */
final class ServerMaxPlayersPlaceholder extends ServerInstancePlaceholder {

    ServerMaxPlayersPlaceholder() {
        super("max_players", 0,
                List.of("Configured maximum number of players, from server.properties"),
                List.of("%server_max_players%"));
    }

    @Override
    protected String resolve(MinecraftServer server) {
        return Integer.toString(server.getMaxPlayers());
    }
}
