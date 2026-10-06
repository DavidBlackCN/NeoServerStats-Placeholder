package io.github.davidblackcn.neoserverstatsplaceholder.player;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

/**
 * Tracks when each online player's current session started.
 *
 * <p>Current-session playtime is deliberately separate from the vanilla cumulative playtime
 * statistic: this is ephemeral runtime state only. Entries are created on login, removed on logout
 * and cleared when the server stops. Nothing is persisted, so every restart resets all sessions, as
 * required.
 *
 * <p>Unusual disconnects are safe by construction: a stale entry is simply overwritten when the same
 * player logs in again, and the map can never grow beyond the set of players that are currently
 * online (missed logout events are also cleaned by the snapshot publisher).
 *
 * <p>Only the server thread accesses this tracker; consumers read the published string snapshot.
 * Values are {@link System#nanoTime()} readings,
 * which are monotonic and unaffected by system clock changes.
 */
public final class PlayerSessionTracker {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    /** Returned when the player has no active session. */
    public static final long NO_SESSION = -1L;

    private final Map<UUID, Long> sessionStartNanos = new HashMap<>();

    /** Starts or restarts the session of a player who just logged in. */
    public void onLogin(UUID playerId) {
        this.sessionStartNanos.put(playerId, System.nanoTime());
    }

    /** Ends the session of a player who logged out. */
    public void onLogout(UUID playerId) {
        this.sessionStartNanos.remove(playerId);
    }

    /** Drops all session state, so nothing leaks into a later server instance. */
    public void clear() {
        this.sessionStartNanos.clear();
    }

    /** Bounds memory even if a consumer or unusual disconnect caused the logout event to be missed. */
    public void retainOnline(java.util.Set<UUID> onlineIds) {
        this.sessionStartNanos.keySet().retainAll(onlineIds);
    }

    /** {@return elapsed seconds of the player's current session, or {@link #NO_SESSION} when none} */
    public long sessionSeconds(UUID playerId) {
        Long startNanos = this.sessionStartNanos.get(playerId);

        if (startNanos == null) {
            return NO_SESSION;
        }

        return (System.nanoTime() - startNanos) / NANOS_PER_SECOND;
    }
}
