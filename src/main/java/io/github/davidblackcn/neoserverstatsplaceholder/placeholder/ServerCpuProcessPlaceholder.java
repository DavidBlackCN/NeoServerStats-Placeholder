package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.NumberFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.CpuMetrics;

/**
 * {@code %server_cpu_process%} — CPU used by this JVM process, for example {@code 37.4%}.
 *
 * <p>Normalised across all processors, following the JDK definition: {@code 100%} would mean every
 * core of the machine was busy running JVM threads. The value is read from a snapshot that is
 * refreshed at most once per second, so repeated resolutions inside that window return the same
 * number.
 */
final class ServerCpuProcessPlaceholder extends ServerPlaceholder {

    private final CpuMetrics cpuMetrics;

    ServerCpuProcessPlaceholder(CpuMetrics cpuMetrics) {
        super("cpu_process", 0,
                List.of("JVM process CPU usage as a percentage of all processors, for example 37.4%"),
                List.of("%server_cpu_process%"));

        this.cpuMetrics = cpuMetrics;
    }

    @Override
    protected String resolve() {
        CpuMetrics.CpuSnapshot snapshot = this.cpuMetrics.snapshot();

        if (!snapshot.hasProcessLoad()) {
            return PlaceholderFallback.NOT_AVAILABLE;
        }

        return NumberFormatter.percent(snapshot.processPercent());
    }
}
