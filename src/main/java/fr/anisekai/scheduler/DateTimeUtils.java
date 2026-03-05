package fr.anisekai.scheduler;

import java.time.Instant;

/**
 * Utility class containing method related to date and time.
 */
public final class DateTimeUtils {

    private DateTimeUtils() {}

    /**
     * Checks if the first {@link Instant} is before or equal to the second one.
     *
     * @param one
     *         the first {@link Instant} to compare
     * @param two
     *         the second {@link Instant} to compare
     *
     * @return {@code true} if {@code one} is before or equal to {@code two}, {@code false} otherwise
     */
    public static boolean isBeforeOrEquals(Instant one, Instant two) {

        return one.isBefore(two) || one.equals(two);
    }

    /**
     * Checks if the first {@link Instant} is after or equal to the second one.
     *
     * @param one
     *         The first {@link Instant} to compare
     * @param two
     *         The second {@link Instant} to compare
     *
     * @return {@code true} if {@code one} is after or equal to {@code two}, otherwise {@code false}
     */
    public static boolean isAfterOrEquals(Instant one, Instant two) {

        return one.isAfter(two) || one.equals(two);
    }

}
