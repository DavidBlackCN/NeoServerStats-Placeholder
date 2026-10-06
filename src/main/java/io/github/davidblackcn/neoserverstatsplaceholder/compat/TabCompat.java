package io.github.davidblackcn.neoserverstatsplaceholder.compat;

import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderCatalog;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderSnapshots;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.event.EventHandler;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import me.neznamy.tab.api.placeholder.Placeholder;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderFallback;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

/** Verified TAB 5.2.1 / 5.5.0 NeoForge releases need explicit registration on ServerStartedEvent. */
public final class TabCompat {
    private final PlaceholderSnapshots snapshots;
    private final Logger logger;
    private final EventHandler<TabLoadEvent> reloadHandler = event -> register();
    private volatile boolean enabled = true;
    private final List<Placeholder> registered = new ArrayList<>();

    public TabCompat(PlaceholderSnapshots snapshots, Logger logger) {
        this.snapshots = snapshots;
        this.logger = logger;
    }
    public void start() {
        TabAPI.getInstance().getEventBus().register(TabLoadEvent.class, reloadHandler);
        register();
    }
    private void register() {
        if (!enabled) return;
        // A reload discards the preceding registry and all of its placeholder objects.
        registered.clear();
        try {
            var manager = TabAPI.getInstance().getPlaceholderManager();
            for (String key : PlaceholderCatalog.SERVER_KEYS) {
                registered.add(manager.registerServerPlaceholder("%server_" + key + "%", 1000,
                        () -> enabled ? snapshots.resolve("server", key, null) : PlaceholderFallback.NOT_AVAILABLE));
            }
            for (String key : PlaceholderCatalog.PLAYER_KEYS) {
                registered.add(manager.registerPlayerPlaceholder("%player_" + key + "%", 250,
                        player -> enabled ? snapshots.resolve("player", key, player.getUniqueId()) : PlaceholderFallback.NOT_AVAILABLE));
            }
            logger.info("Registered {} NeoServerStats placeholders with TAB", registered.size());
        } catch (RuntimeException | LinkageError error) {
            enabled = false;
            for (Placeholder placeholder : registered) {
                try {
                    var manager = TabAPI.getInstance().getPlaceholderManager();
                    if (manager.getPlaceholder(placeholder.getIdentifier()) == placeholder) {
                        manager.unregisterPlaceholder(placeholder);
                    }
                } catch (RuntimeException | LinkageError cleanupFailure) {
                    error.addSuppressed(cleanupFailure);
                    break; // The registry may already be discarded; do not repeat the failure.
                }
            }
            registered.clear();
            logger.warn("TAB adapter disabled after API registration failure", error);
        }
    }
    public void stop() {
        enabled = false;
        var api = TabAPI.getInstance();
        api.getEventBus().unregister(reloadHandler);
        // TAB owns and discards its registry during this same stopping event. Its ordering varies
        // with the installed mods; calling unregisterPlaceholder after its unload throws.
        registered.clear();
    }
}
