package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.NumberFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.MemoryMetrics;

/**
 * {@code %server_memory_percent%} — heap in use as a percentage of the maximum heap.
 *
 * <p>Computed as {@code used / max * 100} from a single heap snapshot, so it is coherent with
 * {@code %server_memory_used%} and {@code %server_memory_max%} when they are resolved together.
 * Reported as {@code N/A} when the JVM defines no maximum heap.
 */
final class ServerMemoryPercentPlaceholder extends ServerPlaceholder {

    ServerMemoryPercentPlaceholder() {
        super("memory_percent", 0,
                List.of("JVM heap used as a percentage of the maximum heap, for example 42.7%"),
                List.of("%server_memory_percent%"));
    }

    @Override
    protected String resolve() {
        MemoryMetrics.HeapUsage heap = MemoryMetrics.captureHeap();

        if (!heap.hasMaximum()) {
            return PlaceholderFallback.NOT_AVAILABLE;
        }

        return NumberFormatter.percent(heap.usedPercentOfMaximum());
    }
}
