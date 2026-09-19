package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.NumberFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.CpuMetrics;

/**
 * {@code %server_cpu_system%} — CPU used by the whole host, for example {@code 61.8%}.
 *
 * <p>On a container this reflects the host rather than the container's own CPU quota, because that
 * is what the JDK reports. The value comes from the same once-per-second cached snapshot as
 * {@code %server_cpu_process%}.
 */
final class ServerCpuSystemPlaceholder extends ServerPlaceholder {

    private final CpuMetrics cpuMetrics;

    ServerCpuSystemPlaceholder(CpuMetrics cpuMetrics) {
        super("cpu_system", 0,
                List.of("Host system CPU usage as a percentage, for example 61.8%"),
                List.of("%server_cpu_system%"));

        this.cpuMetrics = cpuMetrics;
    }

    @Override
    protected String resolve() {
        CpuMetrics.CpuSnapshot snapshot = this.cpuMetrics.snapshot();

        if (!snapshot.hasSystemLoad()) {
            return PlaceholderFallback.NOT_AVAILABLE;
        }

        return NumberFormatter.percent(snapshot.systemPercent());
    }
}
