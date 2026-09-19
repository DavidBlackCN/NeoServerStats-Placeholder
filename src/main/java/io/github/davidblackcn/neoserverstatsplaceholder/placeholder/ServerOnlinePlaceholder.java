package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import net.minecraft.server.MinecraftServer;

/** {@code %server_online%} — number of players currently connected. */
final class ServerOnlinePlaceholder extends ServerInstancePlaceholder {

    ServerOnlinePlaceholder() {
        super("online", 0,
                List.of("Number of players currently connected"),
                List.of("%server_online%"));
    }

    @Override
    protected String resolve(MinecraftServer server) {
        return Integer.toString(server.getPlayerCount());
    }
}
