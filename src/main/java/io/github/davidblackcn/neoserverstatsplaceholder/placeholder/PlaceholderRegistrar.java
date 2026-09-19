package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.envyful.papi.api.manager.AbstractPlaceholderManager;
import com.envyful.papi.api.manager.extensions.PlaceholderExtension;

import io.github.davidblackcn.neoserverstatsplaceholder.compat.ForgePlaceholderApiCompat;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.CpuMetrics;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.UptimeTracker;
import io.github.davidblackcn.neoserverstatsplaceholder.player.PlayerSessionTracker;

import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;

/**
 * Owns this mod's Forge PlaceholderAPI namespaces and performs the single registration step.
 *
 * <p>This is the one place where placeholders are registered and where the shared lifecycle state
 * they read lives. Adding a placeholder means adding one {@link ServerPlaceholder} or
 * {@link PlayerPlaceholder} subclass and one {@code register...} call here.
 */
public final class PlaceholderRegistrar {

    /** Namespace for server-wide statistics; produces placeholders of the form {@code %server_key%}. */
    public static final String SERVER_NAMESPACE = "server";

    /** Namespace for per-player statistics; produces placeholders of the form {@code %player_key%}. */
    public static final String PLAYER_NAMESPACE = "player";

    /**
     * A short sample used for the one-line startup self check. It covers server-independent and
     * server-dependent placeholders plus one player placeholder, which demonstrates the missing
     * player-context fallback because the self check runs without a player.
     *
     * <p>It deliberately excludes {@code %server_tps%} and {@code %server_mspt%}: the self check runs
     * on {@code ServerStartedEvent}, before the first tick has completed, so vanilla has no tick
     * timing to report yet and those two correctly answer {@code N/A} at that moment. Every value in
     * this sample is available at startup.
     */
    public static final String SELF_CHECK_SAMPLE =
            "%server_version% | %server_online%/%server_max_players% | %server_uptime%"
                    + " | %server_cpu_process%/%server_cpu_system% | %server_memory_percent%"
                    + " | %player_name%";

    private static final String[] AUTHORS = { "davidblackcn" };
    private static final String DISPLAY_NAME = "NeoServerStats Placeholder";

    private final Logger logger;
    private final AbstractPlaceholderManager<ServerPlayer> serverManager;
    private final AbstractPlaceholderManager<ServerPlayer> playerManager;
    private final List<PlaceholderExtension<ServerPlayer>> serverPlaceholders = new ArrayList<>();
    private final List<PlaceholderExtension<ServerPlayer>> playerPlaceholders = new ArrayList<>();
    private final UptimeTracker uptimeTracker = new UptimeTracker();
    private final PlayerSessionTracker sessionTracker = new PlayerSessionTracker();
    private final CpuMetrics cpuMetrics = new CpuMetrics();

    public PlaceholderRegistrar(Logger logger, String modVersion) {
        this.logger = logger;
        this.serverManager = ForgePlaceholderApiCompat.newManager(
                SERVER_NAMESPACE, AUTHORS, modVersion, DISPLAY_NAME);
        this.playerManager = ForgePlaceholderApiCompat.newManager(
                PLAYER_NAMESPACE, AUTHORS, modVersion, DISPLAY_NAME);
    }

    /** Registers every placeholder and publishes both namespaces to Forge PlaceholderAPI. */
    public void registerAll() {
        this.registerServerPlaceholders();
        this.registerPlayerPlaceholders();

        ForgePlaceholderApiCompat.register(this.serverManager);
        ForgePlaceholderApiCompat.register(this.playerManager);

        this.logger.info("Registered {} '{}' and {} '{}' placeholders for Forge PlaceholderAPI",
                this.serverPlaceholders.size(), SERVER_NAMESPACE,
                this.playerPlaceholders.size(), PLAYER_NAMESPACE);

        // Reported once at startup rather than per resolution, so an unsupported platform does not
        // produce log spam while its CPU placeholders keep returning the fallback.
        if (!this.cpuMetrics.isSupported()) {
            this.logger.warn("This JVM does not expose the extended operating system management bean;"
                            + " %server_cpu_process% and %server_cpu_system% will report {}",
                    PlaceholderFallback.NOT_AVAILABLE);
        }
    }

    // -------------------------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------------------------

    /** Starts uptime measurement for this server instance. */
    public void onServerStarted() {
        this.uptimeTracker.markStarted();
    }

    /** Records the start of a player's current session. */
    public void onPlayerLoggedIn(UUID playerId) {
        this.sessionTracker.onLogin(playerId);
    }

    /** Ends a player's current session. */
    public void onPlayerLoggedOut(UUID playerId) {
        this.sessionTracker.onLogout(playerId);
    }

    /** Clears all per-instance state so nothing leaks into a later server instance. */
    public void onServerStopping() {
        this.uptimeTracker.reset();
        this.sessionTracker.clear();
    }

    // -------------------------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------------------------

    private void registerServerPlaceholders() {
        this.registerServer(new ServerVersionPlaceholder());
        this.registerServer(new ServerOnlinePlaceholder());
        this.registerServer(new ServerMaxPlayersPlaceholder());
        this.registerServer(new ServerPlayersListPlaceholder());
        this.registerServer(new ServerUptimePlaceholder(this.uptimeTracker));
        this.registerServer(new ServerMotdPlaceholder());
        this.registerServer(new ServerMemoryUsedPlaceholder());
        this.registerServer(new ServerMemoryCommittedPlaceholder());
        this.registerServer(new ServerMemoryMaxPlaceholder());
        this.registerServer(new ServerMemoryPercentPlaceholder());
        this.registerServer(new ServerTpsPlaceholder());
        this.registerServer(new ServerMsptPlaceholder());
        this.registerServer(new ServerCpuProcessPlaceholder(this.cpuMetrics));
        this.registerServer(new ServerCpuSystemPlaceholder(this.cpuMetrics));
    }

    private void registerPlayerPlaceholders() {
        this.registerPlayer(new PlayerNamePlaceholder());
        this.registerPlayer(new PlayerUuidPlaceholder());
        this.registerPlayer(new PlayerPingPlaceholder());
        this.registerPlayer(new PlayerDimensionPlaceholder());
        this.registerPlayer(new PlayerXPlaceholder());
        this.registerPlayer(new PlayerYPlaceholder());
        this.registerPlayer(new PlayerZPlaceholder());
        this.registerPlayer(new PlayerDeathsPlaceholder());
        this.registerPlayer(new PlayerPlaytimePlaceholder());
        this.registerPlayer(new PlayerPlaytimeTicksPlaceholder());
        this.registerPlayer(new PlayerPlaytimeSecondsPlaceholder());
        this.registerPlayer(new PlayerPlaytimeHoursPlaceholder());
        this.registerPlayer(new PlayerSessionTimePlaceholder(this.sessionTracker));
    }

    private void registerServer(PlaceholderExtension<ServerPlayer> placeholder) {
        this.serverPlaceholders.add(placeholder);
        this.serverManager.registerPlaceholder(placeholder);
    }

    private void registerPlayer(PlaceholderExtension<ServerPlayer> placeholder) {
        this.playerPlaceholders.add(placeholder);
        this.playerManager.registerPlaceholder(placeholder);
    }
}
