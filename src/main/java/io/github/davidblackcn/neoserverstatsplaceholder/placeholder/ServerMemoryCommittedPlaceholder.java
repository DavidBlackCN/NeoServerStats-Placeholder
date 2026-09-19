package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.MemoryFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.MemoryMetrics;

/**
 * {@code %server_memory_committed%} — JVM heap currently guaranteed to be available to the process.
 *
 * <p>This is the committed heap, not the host's total memory.
 */
final class ServerMemoryCommittedPlaceholder extends ServerPlaceholder {

    ServerMemoryCommittedPlaceholder() {
        super("memory_committed", 0,
                List.of("JVM heap currently committed to the process, for example 4.00 GiB"),
                List.of("%server_memory_committed%"));
    }

    @Override
    protected String resolve() {
        return MemoryFormatter.formatBytes(MemoryMetrics.captureHeap().committed());
    }
}
