package io.github.davidblackcn.neoserverstatsplaceholder.metrics;

/**
 * Tracks how long the current dedicated-server instance has been running.
 *
 * <p>Wall-clock elapsed time is used rather than tick count so that a lagging server still reports
 * the real time since start. {@link System#nanoTime()} is used instead of
 * {@link System#currentTimeMillis()} because it is monotonic and immune to system clock adjustments.
 *
 * <p>Lifecycle: started on server start, cleared on server stop, never persisted. Uptime therefore
 * resets on every restart, as required. {@code startNanos} is volatile because the lifecycle events
 * run on the server thread while placeholders may be resolved by a consumer thread.
 */
public final class UptimeTracker {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final long NOT_STARTED = -1L;

    private volatile long startNanos = NOT_STARTED;

    /** Records the start of the current server instance. */
    public void markStarted() {
        this.startNanos = System.nanoTime();
    }

    /** Forgets the current server instance. Called when the server stops. */
    public void reset() {
        this.startNanos = NOT_STARTED;
    }

    /** {@return elapsed seconds since the current instance started, or {@code -1} when not running} */
    public long elapsedSeconds() {
        long start = this.startNanos;

        if (start == NOT_STARTED) {
            return NOT_STARTED;
        }

        return (System.nanoTime() - start) / NANOS_PER_SECOND;
    }
}
