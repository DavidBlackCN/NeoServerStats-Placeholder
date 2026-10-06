package io.github.davidblackcn.neoserverstatsplaceholder.compat;

import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderSnapshots;
import java.util.Arrays;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

/** Optional classes are only loaded after checking their mod and exact verified version. */
public final class OptionalIntegrations {
    private final PlaceholderSnapshots snapshots;
    private final Logger logger;
    private TabCompat tab;
    public OptionalIntegrations(PlaceholderSnapshots snapshots, Logger logger) {
        this.snapshots = snapshots;
        this.logger = logger;
    }
    public void setup() {
        if (supported("easybot", "0.3.3")) {
            try {
                EasyBotCompat.register(snapshots, logger);
            } catch (RuntimeException | LinkageError error) {
                logger.warn("EasyBot adapter disabled after API registration failure", error);
            }
        }
    }
    public void start() {
        if (supported("tab", "5.2.1", "5.5.0")) {
            try {
                tab = new TabCompat(snapshots, logger);
                tab.start();
            } catch (RuntimeException | LinkageError error) {
                tab = null;
                logger.warn("TAB adapter disabled after API initialization failure", error);
            }
        }
    }
    public void stop() {
        if (tab != null) {
            try { tab.stop(); }
            catch (RuntimeException | LinkageError error) { logger.warn("Could not detach TAB adapter", error); }
            tab = null;
        }
    }
    private boolean supported(String id, String... expectedVersions) {
        var mod = ModList.get().getModContainerById(id);
        if (mod.isEmpty()) return false;
        String actual = mod.get().getModInfo().getVersion().toString();
        if (Arrays.asList(expectedVersions).contains(actual)) return true;
        logger.warn("{} adapter disabled: verified versions are {}, installed version is {}",
                id, String.join(", ", expectedVersions), actual);
        return false;
    }
}
