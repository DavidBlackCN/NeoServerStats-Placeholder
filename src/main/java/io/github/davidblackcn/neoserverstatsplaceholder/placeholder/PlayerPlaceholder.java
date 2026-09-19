package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;

/**
 * Base class for placeholders published under the {@code player} namespace, i.e.
 * {@code %player_<key>%}.
 *
 * <p>These placeholders describe one specific player, so they only answer on Forge PlaceholderAPI's
 * player path. When the API resolves them without a player context it calls
 * {@code parseObject} instead, which is the <strong>centralized missing-context policy</strong>:
 * it returns {@link PlaceholderFallback#NOT_AVAILABLE} and never throws, never picks an arbitrary
 * online player and never falls back to the first entry of the player list.
 *
 * <p>Implementations must keep {@link #resolve(ServerPlayer)} cheap: no blocking, no I/O, no large
 * allocations.
 */
public abstract class PlayerPlaceholder extends KeyedPlaceholder {

    protected PlayerPlaceholder(String key, int priority, List<String> description, List<String> examples) {
        super(key, priority, description, examples);
    }

    @Override
    public final String parse(ServerPlayer player, String placeholder) {
        return this.resolve(player);
    }

    @Override
    public final String parseObject(Object context, String placeholder) {
        return PlaceholderFallback.NOT_AVAILABLE;
    }

    /** Produces the replacement text for {@code %player_<key>%} from the resolved player. */
    protected abstract String resolve(ServerPlayer player);
}
