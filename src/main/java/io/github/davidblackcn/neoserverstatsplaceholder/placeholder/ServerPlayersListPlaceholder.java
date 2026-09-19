package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code %server_players_list%} — comma-separated names of the connected players, or an empty string
 * when nobody is online.
 *
 * <p>The player list is read directly and is not cached. This is the only placeholder that iterates
 * player state, which the project's performance rules explicitly allow for this one.
 *
 * <p><strong>Threading.</strong> {@code PlayerList#getPlayers()} returns an unmodifiable <em>view</em>
 * over the live player list, so iterating it is only safe while the server thread is not mutating
 * that list. Forge PlaceholderAPI 2.1.0 has no asynchronous resolution path of its own: it exposes
 * {@code onPlaceholderRequest} / {@code UtilPlaceholder#replaceIdentifiers}, and the calling consumer
 * decides the thread. The consumers that exist for this platform resolve synchronously on the server
 * thread, so that is the contract relied on here, matching how Minecraft server state is treated
 * elsewhere in this mod. If a future consumer resolves placeholders from another thread, this is the
 * placeholder that would need a cached snapshot instead.
 */
final class ServerPlayersListPlaceholder extends ServerInstancePlaceholder {

    private static final String SEPARATOR = ", ";

    ServerPlayersListPlaceholder() {
        super("players_list", 0,
                List.of("Comma-separated names of the online players, empty when nobody is online"),
                List.of("%server_players_list%"));
    }

    @Override
    protected String resolve(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        if (players.isEmpty()) {
            return "";
        }

        StringBuilder names = new StringBuilder();

        for (ServerPlayer player : players) {
            if (names.length() > 0) {
                names.append(SEPARATOR);
            }

            // The game profile name is the canonical account name, with no display formatting.
            names.append(player.getGameProfile().getName());
        }

        return names.toString();
    }
}
