package io.github.davidblackcn.neoserverstatsplaceholder.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import io.github.davidblackcn.neoserverstatsplaceholder.format.CompatibilityFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.format.MemoryFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.format.NumberFormatter;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderFallback;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderSnapshots;
import io.github.davidblackcn.neoserverstatsplaceholder.player.PlayerSessionTracker;
import io.github.davidblackcn.neoserverstatsplaceholder.player.PlayerSnapshotCollector;
import net.neoforged.fml.ModList;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** All Minecraft reads occur on the server thread. Consumers only see the published snapshot. */
public final class RuntimeSnapshotService {
    private static final long GLOBAL_REFRESH_NANOS = 1_000_000_000L;
    private final PlaceholderSnapshots snapshots;
    private final UptimeTracker uptime = new UptimeTracker();
    private final PlayerSessionTracker sessions = new PlayerSessionTracker();
    private final CpuMetrics cpu = new CpuMetrics();
    private final RollingTpsMetrics history = new RollingTpsMetrics();
    private Map<String, String> global = Map.of();
    private long lastGlobalRefresh;
    private int ticks;

    public RuntimeSnapshotService(PlaceholderSnapshots snapshots) { this.snapshots = snapshots; }
    public boolean hasCpuSupport() { return cpu.isSupported(); }
    public void start(MinecraftServer server) {
        uptime.markStarted();
        ticks = 0;
        history.start(System.nanoTime());
        refreshGlobal(server);
        refreshPlayers(server);
    }
    public void tick(MinecraftServer server) {
        history.completedTick(System.nanoTime());
        boolean refresh = System.nanoTime() - lastGlobalRefresh >= GLOBAL_REFRESH_NANOS;
        if (refresh) refreshGlobal(server);
        if (++ticks % 5 == 0 || refresh) refreshPlayers(server);
    }
    public void login(ServerPlayer player) {
        sessions.onLogin(player.getUUID());
        refreshGlobal(player.server);
        refreshPlayers(player.server);
    }
    public void logout(ServerPlayer player) {
        sessions.onLogout(player.getUUID());
        refreshGlobal(player.server);
        refreshPlayers(player.server);
    }
    public void stop() {
        sessions.clear();
        uptime.reset();
        history.clear();
        global = Map.of();
        snapshots.clear();
    }
    private void refreshGlobal(MinecraftServer server) {
        Map<String, String> values = new HashMap<>();
        values.put("version", server.getServerVersion());
        // The logout event precedes vanilla list removal; exclude its already-ended session now.
        var onlinePlayers = server.getPlayerList().getPlayers().stream()
                .filter(p -> sessions.sessionSeconds(p.getUUID()) != PlayerSessionTracker.NO_SESSION).toList();
        values.put("online", Integer.toString(onlinePlayers.size()));
        values.put("max_players", Integer.toString(server.getPlayerList().getMaxPlayers()));
        values.put("players_list", String.join(", ", onlinePlayers.stream()
                .map(p -> p.getGameProfile().getName()).toList()));
        values.put("motd", ChatFormatting.stripFormatting(server.getMotd()));
        values.put("uptime", CompatibilityFormatter.uptime(uptime.elapsedSeconds()));
        String neoVersion = ModList.get().getModContainerById("neoforge").orElseThrow().getModInfo().getVersion().toString();
        values.put("name", "A Minecraft Server");
        values.put("variant", "NeoForge");
        values.put("build", neoVersion);
        values.put("version_full", server.getServerVersion() + "-" + neoVersion);
        values.put("has_whitelist", CompatibilityFormatter.bool(server.getPlayerList().isUsingWhitelist()));
        MemoryMetrics.HeapUsage heap = MemoryMetrics.captureHeap();
        values.put("ram_used", Long.toString(heap.used() / 1_048_576L));
        values.put("ram_free", Long.toString((heap.committed() - heap.used()) / 1_048_576L));
        values.put("ram_total", Long.toString(heap.committed() / 1_048_576L));
        values.put("ram_max", heap.hasMaximum() ? Long.toString(heap.max() / 1_048_576L) : unavailable());
        values.put("memory_used", MemoryFormatter.formatBytes(heap.used()));
        values.put("memory_committed", MemoryFormatter.formatBytes(heap.committed()));
        values.put("memory_max", heap.hasMaximum() ? MemoryFormatter.formatBytes(heap.max()) : unavailable());
        values.put("memory_percent", heap.hasMaximum()
                ? NumberFormatter.percent(heap.usedPercentOfMaximum()) : unavailable());
        ServerPerformanceMetrics.TickTiming timing = ServerPerformanceMetrics.measure(server);
        values.put("tps", timing.isAvailable() ? NumberFormatter.twoDecimals(timing.ticksPerSecond()) : unavailable());
        values.put("tps_current", values.get("tps"));
        RollingTpsMetrics.Sample historical = history.sample(System.nanoTime());
        putHistorical(values, "1", historical.oneMinute());
        putHistorical(values, "5", historical.fiveMinutes());
        putHistorical(values, "15", historical.fifteenMinutes());
        values.put("tps", historical.available()
                ? values.get("tps_1_colored") + "\u00a77, " + values.get("tps_5_colored")
                    + "\u00a77, " + values.get("tps_15_colored") : unavailable());
        values.put("mspt", timing.isAvailable() ? NumberFormatter.twoDecimals(timing.millisecondsPerTick()) : unavailable());
        cpu.refresh();
        CpuMetrics.CpuSnapshot sample = cpu.snapshot();
        values.put("cpu_process", sample.hasProcessLoad() ? NumberFormatter.percent(sample.processPercent()) : unavailable());
        values.put("cpu_system", sample.hasSystemLoad() ? NumberFormatter.percent(sample.systemPercent()) : unavailable());
        global = Map.copyOf(values);
        lastGlobalRefresh = System.nanoTime();
    }
    private void refreshPlayers(MinecraftServer server) {
        Map<UUID, Map<String, String>> players = new HashMap<>();
        Map<Object, UUID> contexts = new HashMap<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            // Logout may fire before vanilla removes the player from its list.
            if (sessions.sessionSeconds(id) == PlayerSessionTracker.NO_SESSION) continue;
            players.put(id, PlayerSnapshotCollector.capture(player, sessions.sessionSeconds(id)));
            contexts.put(player, id);
        }
        sessions.retainOnline(players.keySet());
        snapshots.publish(global, players, contexts);
    }
    private static void putHistorical(Map<String, String> values, String window, double tps) {
        values.put("tps_" + window, tps < 0 ? unavailable() : CompatibilityFormatter.tps(tps));
        values.put("tps_" + window + "_colored", tps < 0 ? unavailable() : CompatibilityFormatter.coloredTps(tps));
    }
    private static String unavailable() { return PlaceholderFallback.NOT_AVAILABLE; }
}
