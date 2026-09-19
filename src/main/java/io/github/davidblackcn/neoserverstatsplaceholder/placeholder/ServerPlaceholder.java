package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;

/**
 * Base class for placeholders published under the {@code server} namespace, i.e.
 * {@code %server_<key>%}.
 *
 * <p>A server statistic needs no player, so this class answers on <em>both</em> Forge PlaceholderAPI
 * resolution paths: with a player context and without one. See
 * {@link KeyedPlaceholder} for how FPAPI chooses between them.
 *
 * <p>Implementations must keep {@link #resolve()} cheap: no blocking, no I/O, no large allocations.
 * Resolution may happen on a consumer's own thread, so only cached or inherently thread-safe state
 * may be read.
 */
public abstract class ServerPlaceholder extends KeyedPlaceholder {

    protected ServerPlaceholder(String key, int priority, List<String> description, List<String> examples) {
        super(key, priority, description, examples);
    }

    @Override
    public final String parse(ServerPlayer player, String placeholder) {
        return this.resolve();
    }

    @Override
    public final String parseObject(Object context, String placeholder) {
        return this.resolve();
    }

    /** Produces the replacement text for {@code %server_<key>%}. */
    protected abstract String resolve();
}
