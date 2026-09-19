package io.github.davidblackcn.neoserverstatsplaceholder;

import com.mojang.logging.LogUtils;

import io.github.davidblackcn.neoserverstatsplaceholder.command.PlaceholderDebugCommand;
import io.github.davidblackcn.neoserverstatsplaceholder.compat.ForgePlaceholderApiCompat;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderRegistrar;

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

    /**
     * FML injects the constructor arguments it knows about ({@code IEventBus}, {@code ModContainer},
     * {@code FMLModContainer}, {@code Dist}); only the mod container is needed here.
     */
    public NeoServerStatsPlaceholder(ModContainer modContainer) {
        String modVersion = modContainer.getModInfo().getVersion().toString();

        // Forge PlaceholderAPI keeps a static registry with no lifecycle event of its own, so
        // registering during mod construction is the correct and simplest hook point.
        this.registrar = new PlaceholderRegistrar(LOGGER, modVersion);
        this.registrar.registerAll();

        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, this::onPlayerLoggedOut);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        PlaceholderDebugCommand.register(event.getDispatcher());
    }

    private void onServerStarted(ServerStartedEvent event) {
        this.registrar.onServerStarted();
        this.logSelfCheck(event);
    }

    private void onServerStopping(ServerStoppingEvent event) {
        this.registrar.onServerStopping();
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        this.registrar.onPlayerLoggedIn(event.getEntity().getUUID());
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        this.registrar.onPlayerLoggedOut(event.getEntity().getUUID());
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
