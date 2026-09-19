package io.github.davidblackcn.neoserverstatsplaceholder.format;

/**
 * Formats player block coordinates.
 *
 * <p><strong>v1 contract: always exactly two decimals</strong> ({@code 123.45}), for every consumer
 * and every axis. Integer block coordinates would be shorter but would discard information that
 * cannot be recovered, and consumers can round for display themselves.
 *
 * <p>This is the one place to change when a coordinate precision option is added.
 */
public final class CoordinateFormatter {

    private CoordinateFormatter() {}

    /** Formats one coordinate axis, for example {@code -128.50}. */
    public static String format(double coordinate) {
        return NumberFormatter.twoDecimals(coordinate);
    }
}
