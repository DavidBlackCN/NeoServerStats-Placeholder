package io.github.davidblackcn.neoserverstatsplaceholder.metrics;

import java.lang.management.ManagementFactory;

/**
 * JDK process/operating-environment loads, normalized across available processors (0..100%).
 * Container behavior depends on JVM/platform support; these are not per-core percentages.
 * Only the server-thread publisher samples, at most once per second. Parsing never calls MXBeans.
 */
public final class CpuMetrics {
    private static final long REFRESH_NANOS = 1_000_000_000L;
    private final com.sun.management.OperatingSystemMXBean bean;
    private CpuSnapshot cached = CpuSnapshot.UNAVAILABLE;
    private long sampledAt;
    private boolean sampled;
    public CpuMetrics() {
        var candidate = ManagementFactory.getOperatingSystemMXBean();
        bean = candidate instanceof com.sun.management.OperatingSystemMXBean extended ? extended : null;
    }
    public boolean isSupported() { return bean != null; }
    public void refresh() {
        long now = System.nanoTime();
        if (sampled && now - sampledAt < REFRESH_NANOS) return;
        cached = bean == null ? CpuSnapshot.UNAVAILABLE : new CpuSnapshot(bean.getProcessCpuLoad(), bean.getCpuLoad());
        sampledAt = now;
        sampled = true;
    }
    public CpuSnapshot snapshot() { return cached; }
    public record CpuSnapshot(double processLoad, double systemLoad) {
        public static final CpuSnapshot UNAVAILABLE = new CpuSnapshot(-1, -1);
        public boolean hasProcessLoad() { return Double.isFinite(processLoad) && processLoad >= 0; }
        public boolean hasSystemLoad() { return Double.isFinite(systemLoad) && systemLoad >= 0; }
        public double processPercent() { return Math.clamp(processLoad * 100, 0, 100); }
        public double systemPercent() { return Math.clamp(systemLoad * 100, 0, 100); }
    }
}
