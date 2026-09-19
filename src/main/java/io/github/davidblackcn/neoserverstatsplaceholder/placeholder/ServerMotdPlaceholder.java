package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.List;

import net.minecraft.server.MinecraftServer;

/**
 * {@code %server_motd%} — the configured server message of the day as plain text.
 *
 * <p>Forge PlaceholderAPI works with plain strings and has no rich-text channel, so the vanilla MOTD
 * is returned with its legacy section-sign formatting codes removed. A MOTD such as
 * {@code "\u00a7aWelcome \u00a7rto the server"} therefore resolves to {@code Welcome to the server},
 * which cannot leak colour codes into a consumer's own output.
 */
final class ServerMotdPlaceholder extends ServerInstancePlaceholder {

    /** Legacy Minecraft formatting escape; {@code §} in server.properties. */
    private static final char LEGACY_FORMATTING_MARKER = '\u00a7';

    ServerMotdPlaceholder() {
        super("motd", 0,
                List.of("Server message of the day, as plain text without formatting codes"),
                List.of("%server_motd%"));
    }

    @Override
    protected String resolve(MinecraftServer server) {
        return stripLegacyFormatting(server.getMotd());
    }

    /**
     * Removes legacy formatting codes. A code is the section sign followed by a single character, so
     * the character after each marker is skipped.
     */
    private static String stripLegacyFormatting(String text) {
        int firstMarker = text.indexOf(LEGACY_FORMATTING_MARKER);

        if (firstMarker < 0) {
            return text;
        }

        StringBuilder plainText = new StringBuilder(text.length());

        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);

            if (current == LEGACY_FORMATTING_MARKER) {
                index++;
                continue;
            }

            plainText.append(current);
        }

        return plainText.toString();
    }
}
