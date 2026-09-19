package io.github.davidblackcn.neoserverstatsplaceholder.format;

import java.util.Locale;

/**
 * Formats plain numbers for display.
 *
 * <p>The single place where decimal precision and percentage presentation are defined, so that every
 * placeholder shows numbers in the same shape. {@link Locale#ROOT} is always used, so output never
 * depends on the server's locale (no decimal commas).
 */
public final class NumberFormatter {

    private static final String ONE_DECIMAL_PATTERN = "%.1f";
    private static final String TWO_DECIMAL_PATTERN = "%.2f";

    private NumberFormatter() {}

    /** Formats a value with exactly one decimal, for example {@code 42.7}. */
    public static String oneDecimal(double value) {
        return String.format(Locale.ROOT, ONE_DECIMAL_PATTERN, value);
    }

    /** Formats a value with exactly two decimals, for example {@code 12.43}. */
    public static String twoDecimals(double value) {
        return String.format(Locale.ROOT, TWO_DECIMAL_PATTERN, value);
    }

    /** Formats a percentage with one decimal, for example {@code 42.7%}. */
    public static String percent(double percent) {
        return oneDecimal(percent) + "%";
    }
}
