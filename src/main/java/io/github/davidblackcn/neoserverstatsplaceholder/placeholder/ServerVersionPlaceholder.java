package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import net.minecraft.SharedConstants;

/**
 * {@code %server_version%} — the Minecraft version this server runs, for example {@code 1.21.1}.
 *
 * <p>This was the Phase 1 proof-of-concept placeholder proving that registration and resolution
 * through the real Forge PlaceholderAPI 2.1.0 API work on a dedicated server. It is kept because it
 * is part of the required placeholder set.
 */
final class ServerVersionPlaceholder extends ServerPlaceholder {

    ServerVersionPlaceholder() {
        super("version", 0,
                List.of("Minecraft version of this server, for example 1.21.1"),
                List.of("%server_version%"));
    }

    /**
     * {@link SharedConstants#getCurrentVersion()} is a static constant holder that never changes
     * while the process runs. Using it keeps this placeholder independent of a server instance, so
     * it resolves correctly even during startup.
     *
     * <p>{@code MinecraftServer#getServerVersion()} returns exactly the same string; it is not used
     * here only to avoid requiring a live server for a value that is a build constant.
     */
    @Override
    protected String resolve() {
        return SharedConstants.getCurrentVersion().getName();
    }
}
