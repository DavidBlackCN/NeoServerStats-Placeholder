package io.github.davidblackcn.neoserverstatsplaceholder.metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Reads JVM heap counters through the Java management API.
 *
 * <p>{@link MemoryMXBean} is used instead of {@code Runtime.freeMemory()} because it reports used,
 * committed and maximum heap in one consistent snapshot, and because the semantics are explicit.
 * These values describe the <strong>JVM heap only</strong>; they are not host or container memory,
 * and {@code used} is not "free system memory".
 *
 * <p>The bean reference is resolved once. Each capture is a single MXBean call: no I/O, no blocking
 * and no allocation beyond the small returned record, which keeps it usable from a placeholder
 * handler. Values are deliberately sampled on demand so that the four memory placeholders stay
 * coherent with each other rather than drifting by up to a cache interval.
 */
public final class MemoryMetrics {

    private static final MemoryMXBean MEMORY_BEAN = ManagementFactory.getMemoryMXBean();

    private MemoryMetrics() {}

    /** Captures the current heap usage. Cheap enough to call directly from a placeholder handler. */
    public static HeapUsage captureHeap() {
        MemoryUsage usage = MEMORY_BEAN.getHeapMemoryUsage();
        return new HeapUsage(usage.getUsed(), usage.getCommitted(), usage.getMax());
    }

    /**
     * Immutable snapshot of the JVM heap counters.
     *
     * @param used      heap currently in use, in bytes
     * @param committed heap currently guaranteed to the JVM, in bytes
     * @param max       maximum heap the JVM will attempt to use, in bytes, or {@code -1} when the
     *                  JVM does not define a maximum
     */
    public record HeapUsage(long used, long committed, long max) {

        /** {@code MemoryUsage} reports {@code -1} when no maximum is defined. */
        public boolean hasMaximum() {
            return this.max > 0L;
        }

        /** Heap in use as a percentage of the maximum heap. Only meaningful when {@link #hasMaximum()}. */
        public double usedPercentOfMaximum() {
            return 100.0D * this.used / this.max;
        }
    }
}
