package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.MemoryFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.MemoryMetrics;

/**
 * {@code %server_memory_max%} — maximum heap the JVM will attempt to use.
 *
 * <p>{@code MemoryUsage} reports {@code -1} when no maximum is defined; that is reported as
 * {@code N/A} rather than as a negative size.
 */
final class ServerMemoryMaxPlaceholder extends ServerPlaceholder {

    ServerMemoryMaxPlaceholder() {
        super("memory_max", 0,
                List.of("Maximum JVM heap, or N/A when the JVM defines no maximum"),
                List.of("%server_memory_max%"));
    }

    @Override
    protected String resolve() {
        MemoryMetrics.HeapUsage heap = MemoryMetrics.captureHeap();

        if (!heap.hasMaximum()) {
            return PlaceholderFallback.NOT_AVAILABLE;
        }

        return MemoryFormatter.formatBytes(heap.max());
    }
}
