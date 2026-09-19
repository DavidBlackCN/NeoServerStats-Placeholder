package io.github.davidblackcn.neoserverstatsplaceholder.compat;

import com.envyful.papi.api.PlaceholderFactory;
import com.envyful.papi.api.PlaceholderManager;
import com.envyful.papi.api.manager.AbstractPlaceholderManager;
import com.envyful.papi.api.util.UtilPlaceholder;

import net.minecraft.server.level.ServerPlayer;

/**
 * The only class of this mod that touches Forge PlaceholderAPI directly.
 *
 * <p>Everything used here was verified against Forge PlaceholderAPI 2.1.0 for NeoForge 1.21.1
 * (Maven artifact {@code com.envyful.papi:neo21:2.1.0}, released jar
 * {@code ForgePlaceholderAPI-NeoForge-2.1.0-1.21.1.jar}, mod id {@code forgeplaceholderapi},
 * MIT licensed) by reading its source at tag {@code 2.1.0} and the released jar, not from older
 * 1.12.2 / 1.16.5 examples. In particular:
 *
 * <ul>
 *   <li>Registration is a plain static call, {@link PlaceholderFactory#register(PlaceholderManager)}.
 *       There are no annotations, no {@code META-INF/services} entries and no registry events.</li>
 *   <li>A namespace is an {@link AbstractPlaceholderManager} built from an identifier (the namespace),
 *       an author array, a version, a display name and the platform player class. FPAPI's own
 *       {@code ForgePlaceholderManager} is nothing but that class with {@link ServerPlayer} fixed as
 *       the type argument.</li>
 *   <li>Placeholder text syntax is {@code %<identifier>_<key>%}. The manager compiles the pattern
 *       {@code %(identifier_([a-zA-Z0-9_.&|+\-#]+))%}, so {@code server} + key {@code version} is
 *       written {@code %server_version%}.</li>
 *   <li>Resolution goes through {@link PlaceholderManager#onPlaceholderRequest(Object, String)}, which
 *       dispatches on the runtime class of the context object.</li>
 *   <li>{@link UtilPlaceholder#replaceIdentifiers(Object, String)} is the helper consumers such as
 *       ForgeMenus call to expand placeholders inside a larger text.</li>
 * </ul>
 *
 * <p>This class deliberately compiles against the small {@code com.envyful.papi:api} artifact instead
 * of the fat mod jar. The ForgePlaceholderAPI build shadows the API project into the mod jar without
 * relocating {@code com.envyful.papi.api.*}, so the compiled API surface is identical while the
 * compile classpath stays minimal and free of the fat jar's bundled third-party libraries.
 */
public final class ForgePlaceholderApiCompat {

    /** Mod id of Forge PlaceholderAPI, as declared in its own {@code META-INF/neoforge.mods.toml}. */
    public static final String MOD_ID = "forgeplaceholderapi";

    private ForgePlaceholderApiCompat() {}

    /**
     * Creates a placeholder namespace manager for this platform.
     *
     * @param identifier  the namespace, used literally in {@code %identifier_key%}
     * @param authors     author names shown by FPAPI's {@code /placeholderapi} listing
     * @param version     version shown by FPAPI's {@code /placeholderapi} listing
     * @param displayName human readable name shown by FPAPI's {@code /placeholderapi} listing
     */
    public static AbstractPlaceholderManager<ServerPlayer> newManager(
            String identifier, String[] authors, String version, String displayName) {
        return new AbstractPlaceholderManager<>(identifier, authors, version, displayName, ServerPlayer.class);
    }

    /** Publishes a namespace so that every Forge PlaceholderAPI consumer can resolve it. */
    public static void register(PlaceholderManager<?> manager) {
        PlaceholderFactory.register(manager);
    }

    /**
     * Resolves the placeholders contained in {@code text}, exactly the way a real consumer does.
     *
     * <p>{@code context} must not be {@code null}: Forge PlaceholderAPI inspects the runtime class of
     * the context object to choose between its player and non-player resolution paths, and dereferences
     * it unconditionally. A dedicated-server object that is not a {@link ServerPlayer} (for example a
     * command source or the server itself) takes the non-player path.
     */
    public static String evaluatePlaceholders(Object context, String text) {
        return UtilPlaceholder.replaceIdentifiers(context, text);
    }
}
