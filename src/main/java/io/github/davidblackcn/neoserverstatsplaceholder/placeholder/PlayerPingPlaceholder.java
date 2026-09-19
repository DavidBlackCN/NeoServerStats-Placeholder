package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;

/**
 * {@code %player_ping%} — round-trip latency to the player, in whole milliseconds.
 *
 * <p>Vanilla only refreshes this figure when a keep-alive packet is answered, which happens roughly
 * every 15 seconds, and smooths it as {@code (old * 3 + sample) / 4}. The reported value can
 * therefore be up to about 15 seconds stale, but it is the same number the vanilla player list shows,
 * and it costs nothing to read.
 */
final class PlayerPingPlaceholder extends PlayerPlaceholder {

    PlayerPingPlaceholder() {
        super("ping", 0,
                List.of("Player latency in milliseconds, as shown by the vanilla player list"),
                List.of("%player_ping%"));
    }

    @Override
    protected String resolve(ServerPlayer player) {
        return Integer.toString(player.connection.latency());
    }
}
