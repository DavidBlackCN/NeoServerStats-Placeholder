package io.github.davidblackcn.neoserverstatsplaceholder.metrics;

import net.minecraft.server.MinecraftServer;

/**
 * Derives TPS and MSPT from the tick timing data the vanilla server already maintains.
 *
 * <p><strong>Source.</strong> {@code MinecraftServer#getAverageTickTimeNanos()} is a true rolling
 * average: vanilla keeps the last 100 tick durations in a ring buffer and maintains their running
 * sum, subtracting the evicted sample and adding the new one each tick. Reading it costs nothing and
 * needs no tick listener, no mixin and no state of our own, which is why it is preferred over
 * measuring ticks ourselves.
 *
 * <p><strong>Why TPS is derived from MSPT.</strong> Both values therefore come from the same 100-tick
 * window, which makes them internally coherent by construction rather than by coincidence:
 * {@code TPS = min(ceiling, 1000 / MSPT)}. A tick taking 50 ms yields exactly 20.00 TPS; a tick
 * taking 100 ms yields 10.00 TPS.
 *
 * <p><strong>The ceiling.</strong> A server cannot exceed its configured tick rate, so the ceiling is
 * {@code TickRateManager#tickrate()} (normally 20), capped at 20 to satisfy the display contract.
 * With an operator-reduced rate such as {@code /tick rate 5} a healthy server reports {@code 5.00}
 * instead of a misleading {@code 20.00}; with a raised rate it still reports at most {@code 20.00}.
 */
public final class ServerPerformanceMetrics {

    /** Vanilla targets 20 ticks per second, and TPS is displayed clamped to this value. */
    public static final double MAX_TICKS_PER_SECOND = 20.0D;

    private static final double NANOS_PER_MILLISECOND = 1_000_000.0D;
    private static final double MILLIS_PER_SECOND = 1000.0D;

    private ServerPerformanceMetrics() {}

    /**
     * Reads the current tick timing.
     *
     * <p>The two values are plain field reads on the server (a {@code long} running sum, an
     * {@code int} tick count and a {@code float} tick rate). They are not volatile, so a concurrent
     * read of a value being updated could in principle be inconsistent; the tick duration is only
     * ever a few hundred thousand nanoseconds, so the worst case is a momentarily odd figure and
     * never an exception. Like all server state in this mod, this is treated as server-thread state.
     *
     * @return the current timings, or {@link TickTiming#UNAVAILABLE} before the first tick is
     *         measured
     */
    public static TickTiming measure(MinecraftServer server) {
        long averageTickNanos = server.getAverageTickTimeNanos();

        if (averageTickNanos <= 0L) {
            return TickTiming.UNAVAILABLE;
        }

        double millisecondsPerTick = averageTickNanos / NANOS_PER_MILLISECOND;
        double ceiling = Math.min(MAX_TICKS_PER_SECOND, server.tickRateManager().tickrate());
        double ticksPerSecond = Math.min(ceiling, MILLIS_PER_SECOND / millisecondsPerTick);

        return new TickTiming(millisecondsPerTick, ticksPerSecond);
    }

    /**
     * Immutable tick timing snapshot, taken from one reading of vanilla's rolling window.
     *
     * @param millisecondsPerTick average tick duration
     * @param ticksPerSecond      ticks per second derived from that duration and the tick-rate ceiling
     */
    public record TickTiming(double millisecondsPerTick, double ticksPerSecond) {

        public static final TickTiming UNAVAILABLE = new TickTiming(-1.0D, -1.0D);

        public boolean isAvailable() {
            return this.millisecondsPerTick >= 0.0D;
        }
    }
}
