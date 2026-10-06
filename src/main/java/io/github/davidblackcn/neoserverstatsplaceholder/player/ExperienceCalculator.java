package io.github.davidblackcn.neoserverstatsplaceholder.player;

/** Current spendable XP from level/progress, distinct from vanilla's totalExperience counter. */
public final class ExperienceCalculator {
    private ExperienceCalculator() {}
    public static long currentExperience(int level, float progress, int xpForNextLevel) {
        double l = Math.max(0, level);
        double base = level <= 16 ? l * l + 6 * l
                : level <= 31 ? 2.5 * l * l - 40.5 * l + 360
                : 4.5 * l * l - 162.5 * l + 2220;
        return (long) base + Math.round(Math.max(0, Math.min(1, progress)) * Math.max(0, xpForNextLevel));
    }
}
