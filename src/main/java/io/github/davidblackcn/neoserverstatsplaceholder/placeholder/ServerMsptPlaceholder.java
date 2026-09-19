package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.NumberFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.ServerPerformanceMetrics;

import net.minecraft.server.MinecraftServer;

/**
 * {@code %server_mspt%} — millisecond per tick, averaged over vanilla's 100-tick rolling window.
 *
 * <p>Reported with two decimals, for example {@code 12.43}. It is the same reading that
 * {@code %server_tps%} is derived from, so the two are always coherent.
 */
final class ServerMsptPlaceholder extends ServerInstancePlaceholder {

    ServerMsptPlaceholder() {
        super("mspt", 0,
                List.of("Average milliseconds per tick over the last 100 ticks, for example 12.43"),
                List.of("%server_mspt%"));
    }

    @Override
    protected String resolve(MinecraftServer server) {
        ServerPerformanceMetrics.TickTiming timing = ServerPerformanceMetrics.measure(server);

        if (!timing.isAvailable()) {
            return PlaceholderFallback.NOT_AVAILABLE;
        }

        return NumberFormatter.twoDecimals(timing.millisecondsPerTick());
    }
}
