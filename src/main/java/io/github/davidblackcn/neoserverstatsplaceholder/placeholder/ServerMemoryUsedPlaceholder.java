package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import io.github.davidblackcn.neoserverstatsplaceholder.format.MemoryFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.MemoryMetrics;

/** {@code %server_memory_used%} — JVM heap currently in use, for example {@code 3.42 GiB}. */
final class ServerMemoryUsedPlaceholder extends ServerPlaceholder {

    ServerMemoryUsedPlaceholder() {
        super("memory_used", 0,
                List.of("JVM heap currently in use, for example 3.42 GiB"),
                List.of("%server_memory_used%"));
    }

    @Override
    protected String resolve() {
        return MemoryFormatter.formatBytes(MemoryMetrics.captureHeap().used());
    }
}
