package io.github.davidblackcn.neoserverstatsplaceholder;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import io.github.davidblackcn.neoserverstatsplaceholder.format.CompatibilityFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.CpuMetrics;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderCatalog;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderSnapshots;
import io.github.davidblackcn.neoserverstatsplaceholder.player.ExperienceCalculator;
import io.github.davidblackcn.neoserverstatsplaceholder.player.PlayerSessionTracker;
import org.junit.jupiter.api.Test;

class PlaceholderContractTest {
    @Test void coordinatesAndDefaultFormats() {
        assertEquals("-1", CompatibilityFormatter.blockCoordinate(-0.01));
        assertEquals("-13", CompatibilityFormatter.blockCoordinate(-12.9));
        assertEquals("12", CompatibilityFormatter.blockCoordinate(12.9));
        assertEquals("yes", CompatibilityFormatter.bool(true));
        assertEquals("no", CompatibilityFormatter.bool(false));
        assertEquals("0s", CompatibilityFormatter.uptime(0));
        assertEquals("1w 2d 3h 4m 5s", CompatibilityFormatter.uptime(788645));
        assertEquals("1h", CompatibilityFormatter.uptime(3600));
        assertEquals("\u00a7a50", CompatibilityFormatter.coloredPing(50));
        assertEquals("\u00a7e100", CompatibilityFormatter.coloredPing(100));
        assertEquals("\u00a7c101", CompatibilityFormatter.coloredPing(101));
        assertEquals("\u00a7e18.0", CompatibilityFormatter.coloredTps(18));
        assertEquals("\u00a7a18.0", CompatibilityFormatter.coloredTps(18.1));
        assertEquals("\u00a7c16.0", CompatibilityFormatter.coloredTps(16));
        assertEquals("\u00a7a20.0", CompatibilityFormatter.coloredTps(40));
        assertEquals("Snowy Plains", CompatibilityFormatter.capitalizedBiome("SNOWY_PLAINS"));
        assertEquals("example:custom", CompatibilityFormatter.capitalizedBiome("example:custom"));
        assertEquals("N/A", CompatibilityFormatter.capitalizedBiome("N/A"));
    }
    @Test void experienceLevelBoundariesAndFractionalProgress() {
        int[] levels = {0, 15, 16, 17, 30, 31, 32};
        long[] totals = {0, 315, 352, 394, 1395, 1507, 1628};
        for (int i = 0; i < levels.length; i++) {
            assertEquals(totals[i], ExperienceCalculator.currentExperience(levels[i], 0, 100));
        }
        assertEquals(410, ExperienceCalculator.currentExperience(17, 0.35F, 47));
        assertEquals(0, ExperienceCalculator.currentExperience(-1, -1, -1));
    }
    @Test void missingOfflineUnknownAndImmutableSnapshots() {
        var snapshots = new PlaceholderSnapshots();
        assertEquals("N/A", snapshots.resolve("player", "name", null));
        assertNull(snapshots.resolve("player", "not_implemented", null));
        UUID id = UUID.randomUUID();
        Object context = new Object();
        Map<String, String> player = new HashMap<>(Map.of("name", "Alice"));
        snapshots.publish(Map.of("online", "1"), Map.of(id, player), Map.of(context, id));
        player.put("name", "changed");
        assertEquals("Alice", snapshots.resolveContext("player", "NAME", context));
        assertEquals("N/A", snapshots.resolve("player", "name", UUID.randomUUID()));
        assertEquals("1", snapshots.resolve("server", "online", null));
        snapshots.clear();
        assertEquals("N/A", snapshots.resolveContext("player", "name", context));
    }
    @Test void asyncConsumersReadOnlyPublishedState() {
        var snapshots = new PlaceholderSnapshots();
        UUID id = UUID.randomUUID();
        Object context = new Object();
        var reader = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 10000; i++) {
                String value = snapshots.resolveContext("player", "name", context);
                assertTrue(value.equals("Alice") || value.equals("N/A"));
            }
        });
        for (int i = 0; i < 500; i++) {
            snapshots.publish(Map.of("online", "1"), Map.of(id, Map.of("name", "Alice")), Map.of(context, id));
            snapshots.clear();
        }
        reader.join();
    }
    @Test void sessionsCleanupAndReconnect() {
        var sessions = new PlayerSessionTracker();
        UUID alice = UUID.randomUUID(), bob = UUID.randomUUID();
        assertEquals(-1, sessions.sessionSeconds(alice));
        sessions.onLogin(alice);
        sessions.onLogin(bob);
        assertEquals(0, sessions.sessionSeconds(alice));
        sessions.retainOnline(java.util.Set.of(alice));
        assertEquals(-1, sessions.sessionSeconds(bob));
        sessions.onLogout(alice);
        assertEquals(-1, sessions.sessionSeconds(alice));
        sessions.onLogin(alice);
        assertEquals(0, sessions.sessionSeconds(alice));
        sessions.clear();
        assertEquals(-1, sessions.sessionSeconds(alice));
    }
    @Test void catalogIsUniqueAndUnsupportedCpuIsSafe() {
        assertEquals(30, new HashSet<>(PlaceholderCatalog.SERVER_KEYS).size());
        assertEquals(44, new HashSet<>(PlaceholderCatalog.PLAYER_KEYS).size());
        assertFalse(CpuMetrics.CpuSnapshot.UNAVAILABLE.hasProcessLoad());
        assertFalse(new CpuMetrics.CpuSnapshot(Double.NaN, Double.POSITIVE_INFINITY).hasSystemLoad());
        assertFalse(new CpuMetrics.CpuSnapshot(Double.NaN, 0).hasProcessLoad());
        assertEquals(100, new CpuMetrics.CpuSnapshot(1.5, 0).processPercent());
        var metrics = new CpuMetrics();
        assertSame(CpuMetrics.CpuSnapshot.UNAVAILABLE, metrics.snapshot());
        metrics.refresh();
        var captured = metrics.snapshot();
        metrics.refresh();
        assertSame(captured, metrics.snapshot());
    }
}
