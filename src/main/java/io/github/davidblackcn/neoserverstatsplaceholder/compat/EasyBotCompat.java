package io.github.davidblackcn.neoserverstatsplaceholder.compat;

import com.springwater.easybot.api.IPlaceholderHandler;
import com.springwater.easybot.placeholder.PlaceholderApiMappings;
import com.springwater.easybot.placeholder.PlaceholderManager;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderCatalog;
import io.github.davidblackcn.neoserverstatsplaceholder.placeholder.PlaceholderSnapshots;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

/** Isolated compatibility assumption: EasyBot 0.3.3 exposes a mutable pre-mapping table. */
public final class EasyBotCompat {
    private EasyBotCompat() {}
    public static void register(PlaceholderSnapshots snapshots, Logger logger) {
        var manager = PlaceholderManager.getInstance();
        var activation = new Activation();
        for (String namespace : java.util.List.of("player", "server")) {
            // EasyBot exposes no unregister API. Until BOTH registrations succeed, these handlers
            // stay inert, so a second-prefix conflict cannot partially take over existing mappings.
            manager.registerHandler(new Handler(namespace, snapshots, activation));
        }
        for (String namespace : java.util.List.of("player", "server")) {
            // This runs during common setup, before server/network consumers can resolve tokens.
            // Remove only the exact keys owned by this mod, never clear EasyBot's entire map.
            for (String key : PlaceholderCatalog.keys(namespace)) {
                PlaceholderApiMappings.PLACEHOLDER_API_MAPPINGS.remove("%" + namespace + "_" + key + "%");
            }
        }
        activation.enabled = true;
        logger.info("Registered NeoServerStats handlers with EasyBot 0.3.3");
    }
    private static final class Activation {
        private volatile boolean enabled;
    }
    private record Handler(String namespace, PlaceholderSnapshots snapshots, Activation activation) implements IPlaceholderHandler {
        @Override
        public String getPrefix() { return namespace; }
        @Override
        public String replacePlaceholders(String key, String playerName, ServerPlayer player) {
            return activation.enabled ? snapshots.resolveContext(namespace, key, player) : null;
        }
        @Override
        public String replacePlaceholders(String key, String playerName, MinecraftServer server) {
            return activation.enabled ? snapshots.resolve(namespace, key, null) : null;
        }
    }
}
