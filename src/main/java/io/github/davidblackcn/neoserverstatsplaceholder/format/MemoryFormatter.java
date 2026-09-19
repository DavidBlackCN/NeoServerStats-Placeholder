package io.github.davidblackcn.neoserverstatsplaceholder.format;

import java.util.Locale;

/**
 * Formats JVM heap figures for human display.
 *
 * <p>These are <strong>JVM heap</strong> values, never host or container memory. The single place
 * where heap presentation rules live.
 *
 * <p>Binary units are used because the underlying values come from {@code MemoryUsage}, which is
 * byte based. Values at or above one gibibyte are shown in GiB, everything else in MiB, both with
 * two decimals.
 */
public final class MemoryFormatter {

    private static final long KIBIBYTE = 1024L;
    private static final long MEBIBYTE = 1024L * KIBIBYTE;
    private static final long GIBIBYTE = 1024L * MEBIBYTE;

    private MemoryFormatter() {}

    /** Formats a byte count as {@code 3.42 GiB} or {@code 512.00 MiB}. Negative values become 0. */
    public static String formatBytes(long bytes) {
        long value = Math.max(0L, bytes);

        if (value >= GIBIBYTE) {
            return String.format(Locale.ROOT, "%.2f GiB", value / (double) GIBIBYTE);
        }

        return String.format(Locale.ROOT, "%.2f MiB", value / (double) MEBIBYTE);
    }
}
