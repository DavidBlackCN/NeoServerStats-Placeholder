package io.github.davidblackcn.neoserverstatsplaceholder.metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Samples JVM process and host CPU utilisation from the Java management API.
 *
 * <p><strong>Semantics.</strong> Both figures come from
 * {@code com.sun.management.OperatingSystemMXBean}, the only JDK API that reports CPU
 * utilisation as a percentage:
 *
 * <ul>
 *   <li>{@code getProcessCpuLoad()} — CPU used by this JVM process. Following the JDK definition,
 *       it is normalised across <em>all</em> processors: {@code 1.0} means every core was busy
 *       running JVM threads. {@code 37.4%} therefore means the JVM used 37.4% of the machine's total
 *       CPU capacity, not 37.4% of one core.</li>
 *   <li>{@code getCpuLoad()} — CPU used by the whole host. On a container this reflects the host, not
 *       the container's own cgroup quota.</li>
 * </ul>
 *
 * <p>Both are reported by the JDK as a fraction in {@code [0,1]}, or as a negative value when the
 * platform cannot provide the metric. A negative result is surfaced as "unavailable" and becomes
 * {@code N/A} at the placeholder layer rather than being shown as a bogus number.
 *
 * <p><strong>Why not OSHI or {@code /proc}?</strong> The JDK API covers both figures, so no extra
 * dependency and no OS-specific parsing is needed. Reading {@code /proc} would only work on Linux
 * and is explicitly avoided.
 *
 * <p><strong>Caching.</strong> Samples are refreshed at most once per second. The cached snapshot is
 * only produced when a placeholder actually asks for it — there is no background thread and no work
 * at all while nobody is resolving CPU placeholders.
 */
public final class CpuMetrics {

    private static final long REFRESH_INTERVAL_NANOS = 1_000_000_000L;
    private static final long NOT_SAMPLED = Long.MIN_VALUE;

    /** The JDK reports an unavailable load as a negative fraction. */
    private static final double UNAVAILABLE_LOAD = -1.0D;

    private static final double PERCENT_SCALE = 100.0D;
    private static final double MIN_PERCENT = 0.0D;
    private static final double MAX_PERCENT = 100.0D;

    private final com.sun.management.OperatingSystemMXBean sunBean;
    private final Object sampleLock = new Object();

    private volatile CpuSnapshot cached = CpuSnapshot.UNAVAILABLE;
    private volatile long lastSampleNanos = NOT_SAMPLED;

    public CpuMetrics() {
        OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

        // The concrete bean is com.sun.management.internal.OperatingSystemImpl on HotSpot. The
        // instanceof check keeps this safe on a JVM that only provides the base interface.
        this.sunBean = bean instanceof com.sun.management.OperatingSystemMXBean extended ? extended : null;
    }

    /** {@return whether this JVM exposes the extended management bean at all} */
    public boolean isSupported() {
        return this.sunBean != null;
    }

    /**
     * {@return the cached CPU snapshot, refreshed when the previous sample is at least one second old}
     *
     * <p>The common path is a volatile read plus a subtraction, so placeholder resolution never
     * blocks. Only the refresh takes a short lock, and it is re-checked inside the lock so that
     * concurrent callers cannot stampede.
     */
    public CpuSnapshot snapshot() {
        long now = System.nanoTime();
        long last = this.lastSampleNanos;

        if (last == NOT_SAMPLED || now - last >= REFRESH_INTERVAL_NANOS) {
            synchronized (this.sampleLock) {
                if (this.lastSampleNanos == NOT_SAMPLED
                        || System.nanoTime() - this.lastSampleNanos >= REFRESH_INTERVAL_NANOS) {
                    this.cached = this.sample();
                    this.lastSampleNanos = System.nanoTime();
                }
            }
        }

        return this.cached;
    }

    private CpuSnapshot sample() {
        if (this.sunBean == null) {
            return CpuSnapshot.UNAVAILABLE;
        }

        return new CpuSnapshot(this.sunBean.getProcessCpuLoad(), this.sunBean.getCpuLoad());
    }

    private static double clampPercent(double percent) {
        return Math.max(MIN_PERCENT, Math.min(MAX_PERCENT, percent));
    }

    /**
     * Immutable CPU sample.
     *
     * @param processLoad CPU load of this JVM process as a fraction in {@code [0,1]}, negative when
     *                    unavailable
     * @param systemLoad  CPU load of the whole host as a fraction in {@code [0,1]}, negative when
     *                    unavailable
     */
    public record CpuSnapshot(double processLoad, double systemLoad) {

        public static final CpuSnapshot UNAVAILABLE = new CpuSnapshot(UNAVAILABLE_LOAD, UNAVAILABLE_LOAD);

        public boolean hasProcessLoad() {
            return this.processLoad >= 0.0D;
        }

        public boolean hasSystemLoad() {
            return this.systemLoad >= 0.0D;
        }

        /** Process CPU as a clamped percentage, normalised across all processors. */
        public double processPercent() {
            return clampPercent(this.processLoad * PERCENT_SCALE);
        }

        /** Host CPU as a clamped percentage. */
        public double systemPercent() {
            return clampPercent(this.systemLoad * PERCENT_SCALE);
        }
    }
}
