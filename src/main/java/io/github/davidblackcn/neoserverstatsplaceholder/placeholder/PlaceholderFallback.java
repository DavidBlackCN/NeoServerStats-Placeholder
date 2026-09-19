package io.github.davidblackcn.neoserverstatsplaceholder.placeholder;

/**
 * The single place where this mod's fallback value is defined.
 *
 * <p>Every placeholder that cannot produce a value for a non-error reason (no server instance yet,
 * a metric the JVM does not define, no player context) returns {@link #NOT_AVAILABLE} rather than
 * throwing, returning an empty string, or silently substituting unrelated data. Keeping the literal
 * here means consumers see one consistent value everywhere.
 */
public final class PlaceholderFallback {

    /** Returned when a metric is genuinely unavailable. */
    public static final String NOT_AVAILABLE = "N/A";

    private PlaceholderFallback() {}
}
