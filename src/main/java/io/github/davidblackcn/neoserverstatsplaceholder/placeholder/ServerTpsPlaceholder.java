package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.NumberFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.ServerPerformanceMetrics;

import net.minecraft.server.MinecraftServer;

/**
 * {@code %server_tps%} — smoothed ticks per second, for example {@code 19.98}.
 *
 * <p>Derived from the same vanilla 100-tick rolling window as {@code %server_mspt%}, as
 * {@code min(ceiling, 1000 / MSPT)}, so the two values can never contradict each other. Always
 * reported in the {@code 0.00 .. 20.00} range.
 */
final class ServerTpsPlaceholder extends ServerInstancePlaceholder {

    ServerTpsPlaceholder() {
        super("tps", 0,
                List.of("Smoothed server ticks per second, clamped to 0.00 .. 20.00, for example 19.98"),
                List.of("%server_tps%"));
    }

    @Override
    protected String resolve(MinecraftServer server) {
        ServerPerformanceMetrics.TickTiming timing = ServerPerformanceMetrics.measure(server);

        if (!timing.isAvailable()) {
            return PlaceholderFallback.NOT_AVAILABLE;
        }

        return NumberFormatter.twoDecimals(timing.ticksPerSecond());
    }
}
