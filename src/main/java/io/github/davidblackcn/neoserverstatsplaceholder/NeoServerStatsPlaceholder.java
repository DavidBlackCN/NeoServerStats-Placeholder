package io.github.davidblackcn.neoserverstatsplaceholder;

import com.mojang.logging.LogUtils;

import io.github.davidblackcn.neoserverstatsplaceholder.command.PlaceholderDebugCommand;
import io.github.davidblackcn.neoserverstatsplaceholder.compat.ForgePlaceholderApiCompat;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderRegistrar;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderSnapshots;
import io.github.davidblackcn.neoserverstatsplaceholder.metrics.RuntimeSnapshotService;
import net.minecraft.server.level.ServerPlayer;
import io.github.davidblackcn.neoserverstatsplaceholder.compat.OptionalIntegrations;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import org.slf4j.Logger;

/**
 * Entry point of NeoServerStats Placeholder.
 *
 * <p>This mod is dedicated-server only. {@code dist = Dist.DEDICATED_SERVER} makes FML skip this
 * entrypoint entirely on a client, so no client-only class is ever loaded and no client support is
 * implied. The mod metadata declares every dependency as server-side for the same reason.
 */
@Mod(value = NeoServerStatsPlaceholder.MOD_ID, dist = Dist.DEDICATED_SERVER)
public final class NeoServerStatsPlaceholder {

    /** Must match {@code mod_id} in gradle.properties and {@code modId} in neoforge.mods.toml. */
    public static final String MOD_ID = "neoserverstats_placeholder";

    public static final Logger LOGGER = LogUtils.getLogger();

    private final PlaceholderRegistrar registrar;
    private final PlaceholderSnapshots snapshots = new PlaceholderSnapshots();
    private final RuntimeSnapshotService metrics = new RuntimeSnapshotService(snapshots);
    private final OptionalIntegrations integrations = new OptionalIntegrations(snapshots, LOGGER);

    /**
     * FML injects the constructor arguments it knows about ({@code IEventBus}, {@code ModContainer},
     * {@code FMLModContainer}, {@code Dist}); the bus schedules optional adapters after mod loading.
     */
    public NeoServerStatsPlaceholder(ModContainer modContainer, IEventBus modBus) {
        String modVersion = modContainer.getModInfo().getVersion().toString();

        // Forge PlaceholderAPI keeps a static registry with no lifecycle event of its own, so
        // registering during mod construction is the correct and simplest hook point.
        this.registrar = new PlaceholderRegistrar(snapshots, LOGGER, modVersion);
        this.registrar.registerAll();
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(integrations::setup));
        if (!metrics.hasCpuSupport()) {
            LOGGER.warn("Extended CPU management bean unavailable; CPU placeholders return N/A");
        }

        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, this::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> metrics.tick(event.getServer()));
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        PlaceholderDebugCommand.register(event.getDispatcher());
    }

    private void onServerStarted(ServerStartedEvent event) {
        this.metrics.start(event.getServer());
        integrations.start();
        this.logSelfCheck(event);
    }

    private void onServerStopping(ServerStoppingEvent event) {
        integrations.stop();
        this.metrics.stop();
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) metrics.login(player);
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) metrics.logout(player);
    }

    /**
     * One-shot startup self check. It resolves a short sample through exactly the same path a real
     * Forge PlaceholderAPI consumer uses, so a broken integration is visible in the log instead of
     * silently returning unresolved text. Cost is one string lookup per boot; nothing is logged per
     * placeholder resolution.
     */
    private void logSelfCheck(ServerStartedEvent event) {
        LOGGER.info("Placeholder self check: {} -> {}",
                PlaceholderRegistrar.SELF_CHECK_SAMPLE,
                ForgePlaceholderApiCompat.evaluatePlaceholders(event.getServer(), PlaceholderRegistrar.SELF_CHECK_SAMPLE));
    }
}
