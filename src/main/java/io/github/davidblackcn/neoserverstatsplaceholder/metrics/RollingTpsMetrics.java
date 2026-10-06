package io.github.davidblackcn.neoserverstatsplaceholder.metrics;

import java.util.Arrays;

/**
 * Wall-clock tick throughput for 60/300/900 seconds, independent of vanilla's work-time MSPT.
 * A fixed 901-slot ring counts tick completions in one-second buckets. Boundary buckets are
 * weighted by their overlapping duration (uniform distribution within that second). This bounds
 * storage and tick cost even during /tick sprint; only the once-per-second publisher sums buckets.
 */
public final class RollingTpsMetrics {
    private static final long SECOND = 1_000_000_000L;
    private static final int CAPACITY = 901;
    private final long[] bucketNumbers = new long[CAPACITY];
    private final long[] counts = new long[CAPACITY];
    private long started;
    private boolean running;
    private boolean hasTick;

    public void start(long now) {
        clear();
        started = now;
        running = true;
    }
    public void clear() {
        Arrays.fill(bucketNumbers, -1);
        Arrays.fill(counts, 0);
        running = false;
        hasTick = false;
    }
    public void completedTick(long now) {
        if (!running) return;
        long elapsed = now - started;
        if (elapsed <= 0) return;
        // A tick completed at exactly 1s belongs to (0s, 1s], not the next bucket.
        long bucket = (elapsed - 1) / SECOND;
        int index = (int) (bucket % CAPACITY);
        if (bucketNumbers[index] != bucket) {
            bucketNumbers[index] = bucket;
            counts[index] = 0;
        }
        counts[index]++;
        hasTick = true;
    }
    public Sample sample(long now) {
        long elapsed = now - started;
        if (!running || !hasTick || elapsed <= 0) return Sample.UNAVAILABLE;
        return new Sample(rate(elapsed, 60), rate(elapsed, 300), rate(elapsed, 900));
    }
    private double rate(long elapsed, int windowSeconds) {
        long windowStart = Math.max(0, elapsed - windowSeconds * SECOND);
        double count = 0;
        for (int i = 0; i < CAPACITY; i++) {
            if (bucketNumbers[i] < 0) continue;
            long start = bucketNumbers[i] * SECOND;
            long end = Math.min(start + SECOND, elapsed);
            long overlap = Math.min(end, elapsed) - Math.max(start, windowStart);
            if (overlap > 0 && end > start) count += counts[i] * (overlap / (double) (end - start));
        }
        return Math.clamp(count * SECOND / (elapsed - windowStart), 0, 20);
    }
    public record Sample(double oneMinute, double fiveMinutes, double fifteenMinutes) {
        public static final Sample UNAVAILABLE = new Sample(-1, -1, -1);
        public boolean available() { return oneMinute >= 0; }
    }
}
