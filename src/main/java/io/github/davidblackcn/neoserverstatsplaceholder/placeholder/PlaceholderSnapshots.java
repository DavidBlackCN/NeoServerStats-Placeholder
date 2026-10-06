package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Atomically publishes immutable, preformatted values. Resolvers never access game state. */
public final class PlaceholderSnapshots {
    private volatile Snapshot current = Snapshot.EMPTY;

    public void publish(Map<String, String> server, Map<UUID, Map<String, String>> players,
                        Map<Object, UUID> contexts) {
        Map<UUID, Map<String, String>> immutablePlayers = new java.util.HashMap<>();
        players.forEach((id, values) -> immutablePlayers.put(id, Map.copyOf(values)));
        current = new Snapshot(Map.copyOf(server), Map.copyOf(immutablePlayers), Map.copyOf(contexts));
    }
    public void clear() {
        current = Snapshot.EMPTY;
    }
    /** Unknown keys return null, allowing the consumer to leave the token unchanged. */
    public String resolve(String namespace, String key, UUID playerId) {
        return resolve(current, namespace, key, playerId);
    }
    /** Player object identities are captured on the server thread, avoiding even Entity UUID calls. */
    public String resolveContext(String namespace, String key, Object context) {
        Snapshot snapshot = current;
        UUID id = context == null ? null : snapshot.contexts().get(context);
        return resolve(snapshot, namespace, key, id);
    }
    private static String resolve(Snapshot snapshot, String namespace, String key, UUID playerId) {
        String normalized = key.toLowerCase(Locale.ROOT);
        if (!PlaceholderCatalog.keys(namespace).contains(normalized)) {
            return null;
        }
        Map<String, String> values = "server".equals(namespace) ? snapshot.server()
                : playerId == null ? null : snapshot.players().get(playerId);
        return values == null ? PlaceholderFallback.NOT_AVAILABLE
                : values.getOrDefault(normalized, PlaceholderFallback.NOT_AVAILABLE);
    }
    private record Snapshot(Map<String, String> server, Map<UUID, Map<String, String>> players,
                            Map<Object, UUID> contexts) {
        private static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of(), Map.of());
    }
}
