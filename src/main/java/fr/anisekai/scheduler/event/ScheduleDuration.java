package fr.anisekai.scheduler.event;

import fr.anisekai.scheduler.event.interfaces.entities.WatchTarget;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Calculates the effective duration of a scheduled watch session.
 */
public final class ScheduleDuration {

    private static final int SKIPPED_MINUTES_PER_ADDITIONAL_EPISODE = 3;

    private ScheduleDuration() {

    }

    /**
     * Calculate the runtime for a number of episodes, accounting for optional opening and ending skips.
     *
     * @param target
     *         Target providing the duration of one episode, in minutes.
     * @param episodeCount
     *         Number of episodes in the session.
     * @param skipEnabled
     *         Whether repeated opening and ending sequences are skipped.
     *
     * @return The effective session duration.
     */
    public static @NotNull Duration calculate(@NotNull WatchTarget target, int episodeCount, boolean skipEnabled) {

        long totalRuntime = Math.multiplyExact((long) target.getEpisodeDuration(), episodeCount);
        long skippedRuntime = skipEnabled
                ? Math.multiplyExact((long) episodeCount - 1, SKIPPED_MINUTES_PER_ADDITIONAL_EPISODE)
                : 0;

        return Duration.ofMinutes(Math.subtractExact(totalRuntime, skippedRuntime));
    }

}
