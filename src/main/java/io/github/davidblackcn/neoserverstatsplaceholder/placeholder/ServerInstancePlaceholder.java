package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import net.minecraft.server.MinecraftServer;

import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Base class for {@code %server_*%} placeholders that need the running dedicated server.
 *
 * <p>The "there is no server right now" fallback is centralised here instead of being repeated by
 * every placeholder that touches server state.
 *
 * <p>{@link ServerLifecycleHooks#getCurrentServer()} is NeoForge's own cached reference to the
 * current server: NeoForge populates it before the server starts and clears it once it has stopped.
 * It is the same accessor Forge PlaceholderAPI's own platform manager uses, and it is cheaper and
 * less error-prone than maintaining a second copy of the same reference.
 */
public abstract class ServerInstancePlaceholder extends ServerPlaceholder {

    protected ServerInstancePlaceholder(String key, int priority, List<String> description, List<String> examples) {
        super(key, priority, description, examples);
    }

    @Override
    protected final String resolve() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server == null) {
            return PlaceholderFallback.NOT_AVAILABLE;
        }

        return this.resolve(server);
    }

    /** Produces the replacement text for {@code %server_<key>%} from the running server. */
    protected abstract String resolve(MinecraftServer server);
}
