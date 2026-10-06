package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;
import com.envyful.papi.api.manager.extensions.AbstractExtension;
import io.github.davidblackcn.neoserverstatsplaceholder.compat.ForgePlaceholderApiCompat;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

/** Registers the catalog once; all consumer threads read immutable snapshots. */
public final class PlaceholderRegistrar {
    public static final String SELF_CHECK_SAMPLE =
            "%server_version% | %server_online%/%server_max_players% | %server_uptime% | %player_name%";
    private final PlaceholderSnapshots snapshots;
    private final Logger logger;
    private final String version;

    public PlaceholderRegistrar(PlaceholderSnapshots snapshots, Logger logger, String version) {
        this.snapshots = snapshots;
        this.logger = logger;
        this.version = version;
    }
    public void registerAll() {
        for (String namespace : List.of("server", "player")) {
            var manager = ForgePlaceholderApiCompat.newManager(namespace,
                    new String[] {"davidblackcn"}, version, "NeoServerStats Placeholder");
            for (String key : PlaceholderCatalog.keys(namespace)) {
                manager.registerPlaceholder(new SnapshotExtension(namespace, key, snapshots));
            }
            ForgePlaceholderApiCompat.register(manager);
        }
        logger.info("Registered {} server and {} player placeholders for Forge PlaceholderAPI",
                PlaceholderCatalog.SERVER_KEYS.size(), PlaceholderCatalog.PLAYER_KEYS.size());
    }
    private static final class SnapshotExtension extends AbstractExtension<ServerPlayer> {
        private final String namespace;
        private final PlaceholderSnapshots snapshots;
        private SnapshotExtension(String namespace, String key, PlaceholderSnapshots snapshots) {
            super(key, 0, List.of("Cached " + namespace + " " + key), List.of("%" + namespace + "_" + key + "%"));
            this.namespace = namespace;
            this.snapshots = snapshots;
        }
        @Override
        public boolean matches(ServerPlayer player, String key) { return getName().equalsIgnoreCase(key); }
        @Override
        public boolean matchesObject(Object context, String key) { return getName().equalsIgnoreCase(key); }
        @Override
        public String parse(ServerPlayer player, String key) { return snapshots.resolveContext(namespace, key, player); }
        @Override
        public String parseObject(Object context, String key) { return snapshots.resolve(namespace, key, null); }
    }
}
