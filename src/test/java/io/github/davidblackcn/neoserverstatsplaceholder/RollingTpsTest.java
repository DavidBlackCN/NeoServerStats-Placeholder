package io.github.davidblackcn.neoserverstatsplaceholder;

import static org.junit.jupiter.api.Assertions.*;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.RollingTpsMetrics;
import org.junit.jupiter.api.Test;

class RollingTpsTest {
    @Test void warmupLoadAndIndependentWindows() {
        var metrics = new RollingTpsMetrics();
        metrics.start(0);
        assertFalse(metrics.sample(0).available());
        for (long tick = 1; tick <= 12000; tick++) metrics.completedTick(tick * 50_000_000L);
        var healthy = metrics.sample(600_000_000_000L);
        assertEquals(20, healthy.oneMinute(), 0.001);
        assertEquals(20, healthy.fifteenMinutes(), 0.001);
        for (long tick = 1; tick <= 600; tick++) metrics.completedTick(600_000_000_000L + tick * 100_000_000L);
        var loaded = metrics.sample(660_000_000_000L);
        assertEquals(10, loaded.oneMinute(), 0.001);
        assertEquals(18, loaded.fiveMinutes(), 0.001);
        assertEquals(12600.0 / 660, loaded.fifteenMinutes(), 0.001);
        var betweenTicks = metrics.sample(660_500_000_000L);
        assertEquals(595.0 / 60, betweenTicks.oneMinute(), 0.001);
        assertEquals(5390.0 / 300, betweenTicks.fiveMinutes(), 0.001);
        assertEquals(12600.0 / 660.5, betweenTicks.fifteenMinutes(), 0.001);
        assertEquals(0, metrics.sample(1_600_000_000_000L).fifteenMinutes(), 0.001);
        metrics.clear();
        assertFalse(metrics.sample(1_600_000_000_000L).available());
        metrics.start(2_000_000_000_000L);
        metrics.completedTick(2_000_050_000_000L);
        assertEquals(20, metrics.sample(2_000_050_000_000L).oneMinute(), 0.001);
    }
    @Test void partialBucketsRingWrapAndDisplayCap() {
        var metrics = new RollingTpsMetrics();
        metrics.start(-500_000_000L);
        for (long tick = 1; tick <= 40000; tick++) metrics.completedTick(-500_000_000L + tick * 50_000_000L);
        assertEquals(20, metrics.sample(1_999_500_000_000L).fifteenMinutes(), 0.001);
        for (long tick = 1; tick <= 1000; tick++) metrics.completedTick(1_999_500_000_000L + tick * 1_000_000L);
        assertEquals(20, metrics.sample(2_000_500_000_000L).oneMinute(), 0.001);
    }
}
