package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import com.envyful.papi.api.manager.extensions.AbstractExtension;

import net.minecraft.server.level.ServerPlayer;

/**
 * Base class for this mod's placeholders: owns the placeholder key and the one way a key is matched.
 *
 * <p>Forge PlaceholderAPI 2.1.0 resolves the text {@code %namespace_key%} in
 * {@code AbstractPlaceholderManager#onPlaceholderRequest(Object, String)} and chooses one of two
 * paths based on the <em>runtime class</em> of the context object:
 *
 * <ul>
 *   <li>if the context is a {@link ServerPlayer}, {@code matches} and {@code parse} are called;</li>
 *   <li>otherwise {@code matchesObject} and {@code parseObject} are called. Both are {@code default}
 *       methods on FPAPI's {@code PlaceholderExtension} that match nothing and return {@code null}.</li>
 * </ul>
 *
 * <p>Matching is identical for every placeholder in this mod, so it is implemented once here.
 * Subclasses decide only what each of the two resolution paths returns.
 *
 * <p>Key matching is case-insensitive, following Forge PlaceholderAPI's own {@code SimpleExtension}.
 * The namespace itself stays case-sensitive because it is a literal in FPAPI's regex.
 */
public abstract class KeyedPlaceholder extends AbstractExtension<ServerPlayer> {

    protected KeyedPlaceholder(String key, int priority, List<String> description, List<String> examples) {
        super(key, priority, description, examples);
    }

    @Override
    public final boolean matches(ServerPlayer player, String placeholder) {
        return this.matchesKey(placeholder);
    }

    @Override
    public final boolean matchesObject(Object context, String placeholder) {
        return this.matchesKey(placeholder);
    }

    private boolean matchesKey(String placeholder) {
        return this.getName().equalsIgnoreCase(placeholder);
    }
}
