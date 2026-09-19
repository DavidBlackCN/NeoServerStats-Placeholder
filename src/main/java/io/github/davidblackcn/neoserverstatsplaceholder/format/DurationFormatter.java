package io.github.davidblackcn.neoserverstatsplaceholder.format;

import java.util.Locale;

/**
 * Formats elapsed durations for human display.
 *
 * <p>This is the single place where duration presentation and the tick/time ratio live, so that
 * every placeholder reports time in the same shape.
 *
 * <p>The uptime shape is:
 *
 * <ul>
 *   <li>under one minute: {@code 42s}</li>
 *   <li>under one hour: {@code 18m 07s}</li>
 *   <li>under one day: {@code 4h 23m}</li>
 *   <li>one day or more: {@code 3d 08h 15m}</li>
 * </ul>
 *
 * <p>The session shape always includes seconds, because a session is short and its seconds matter:
 * {@code 33s}, {@code 17m 33s}, {@code 2h 17m 33s}, {@code 1d 02h 17m 33s}.
 */
public final class DurationFormatter {

    /**
     * Minecraft runs 20 game ticks per second, and vanilla stores its time statistics
     * ({@code minecraft:play_time}) in ticks: {@code StatFormatter.TIME} divides the raw value by 20
     * to obtain seconds. The ratio is defined once, here, next to the code that consumes it.
     */
    public static final long TICKS_PER_SECOND = 20L;

    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long SECONDS_PER_HOUR = 60L * SECONDS_PER_MINUTE;
    private static final long SECONDS_PER_DAY = 24L * SECONDS_PER_HOUR;
    private static final long HOURS_PER_DAY = 24L;

    private DurationFormatter() {}

    /**
     * Formats a duration that is reported at second granularity.
     *
     * @param totalSeconds elapsed seconds; negative values are treated as zero
     */
    public static String formatUptime(long totalSeconds) {
        long seconds = Math.max(0L, totalSeconds);

        if (seconds < SECONDS_PER_MINUTE) {
            return seconds + "s";
        }

        if (seconds < SECONDS_PER_HOUR) {
            return String.format(Locale.ROOT, "%dm %02ds",
                    seconds / SECONDS_PER_MINUTE,
                    seconds % SECONDS_PER_MINUTE);
        }

        if (seconds < SECONDS_PER_DAY) {
            return String.format(Locale.ROOT, "%dh %02dm",
                    seconds / SECONDS_PER_HOUR,
                    (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
        }

        return String.format(Locale.ROOT, "%dd %02dh %02dm",
                seconds / SECONDS_PER_DAY,
                (seconds % SECONDS_PER_DAY) / SECONDS_PER_HOUR,
                (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
    }

    /**
     * Formats a single login session.
     *
     * @param totalSeconds session length in seconds; negative values are treated as zero
     */
    public static String formatSession(long totalSeconds) {
        long seconds = Math.max(0L, totalSeconds);
        long totalHours = seconds / SECONDS_PER_HOUR;
        long minutes = (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        long remainingSeconds = seconds % SECONDS_PER_MINUTE;

        if (totalHours == 0L) {
            if (minutes == 0L) {
                return remainingSeconds + "s";
            }

            return String.format(Locale.ROOT, "%dm %02ds", minutes, remainingSeconds);
        }

        long days = totalHours / HOURS_PER_DAY;
        long hours = totalHours % HOURS_PER_DAY;

        if (days == 0L) {
            return String.format(Locale.ROOT, "%dh %02dm %02ds", hours, minutes, remainingSeconds);
        }

        return String.format(Locale.ROOT, "%dd %02dh %02dm %02ds", days, hours, minutes, remainingSeconds);
    }

    /** Converts a vanilla tick counter to whole seconds. Negative tick counts are treated as zero. */
    public static long secondsFromTicks(long ticks) {
        return Math.max(0L, ticks) / TICKS_PER_SECOND;
    }

    /** Converts a vanilla tick counter to hours, for the {@code 108.58} style of value. */
    public static double hoursFromTicks(long ticks) {
        return Math.max(0L, ticks) / (double) TICKS_PER_SECOND / SECONDS_PER_HOUR;
    }

    /** Formats a number of hours with two decimals, for example {@code 108.58}. */
    public static String formatHours(double hours) {
        return NumberFormatter.twoDecimals(hours);
    }
}
