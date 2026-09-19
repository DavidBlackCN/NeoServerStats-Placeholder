package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.DurationFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.UptimeTracker;

/**
 * {@code %server_uptime%} — time since this server instance started, for example {@code 4h 23m}.
 *
 * <p>Reports wall-clock time rather than elapsed ticks, so a lagging server still shows the real
 * time it has been up. The value comes from {@link UptimeTracker}, which is pure in-memory state and
 * needs no server reference.
 */
final class ServerUptimePlaceholder extends ServerPlaceholder {

    private final UptimeTracker uptimeTracker;

    ServerUptimePlaceholder(UptimeTracker uptimeTracker) {
        super("uptime", 0,
                List.of("Time since this server instance started, for example 4h 23m"),
                List.of("%server_uptime%"));

        this.uptimeTracker = uptimeTracker;
    }

    @Override
    protected String resolve() {
        long elapsedSeconds = this.uptimeTracker.elapsedSeconds();

        if (elapsedSeconds < 0L) {
            return PlaceholderFallback.NOT_AVAILABLE;
        }

        return DurationFormatter.formatUptime(elapsedSeconds);
    }
}
