package fr.anisekai.scheduler.event.data;

import fr.anisekai.scheduler.event.interfaces.ScheduleSpotData;
import fr.anisekai.scheduler.event.interfaces.entities.Planifiable;
import fr.anisekai.scheduler.event.interfaces.entities.WatchTarget;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

/**
 * Represent a reserved spot in an event schedule, that can be converted to {@link Planifiable}.
 *
 * @param watchTarget
 *         The {@link WatchTarget} of the spot.
 * @param startingAt
 *         When the spot starts.
 * @param firstEpisode
 *         The first episode that will be watched.
 * @param episodeCount
 *         The amount of episode that will be watched.
 * @param skipEnabled
 *         If skipping any superfluous part is enabled.
 * @param <T>
 *         Type of the {@link WatchTarget}.
 */
public record ReservedSpot<T extends WatchTarget>(
        @NotNull T watchTarget,
        @NotNull Instant startingAt,
        int firstEpisode,
        int episodeCount,
        boolean skipEnabled
) {

    /**
     * Create a {@link ReservedSpot} from the provided {@link ScheduleSpotData}.
     *
     * @param spot
     *         The {@link ScheduleSpotData} from which the {@link ReservedSpot} will be created.
     * @param <T>
     *         Type of the {@link WatchTarget}
     *
     * @return A {@link ReservedSpot}.
     */
    @Contract(value = "_ -> new", pure = true)
    public static <T extends WatchTarget> @NotNull ReservedSpot<T> of(@NotNull ScheduleSpotData<T> spot) {

        return of(spot, spot.getWatchTarget().getWatched() + 1);
    }

    /**
     * Create a {@link ReservedSpot} from the provided {@link ScheduleSpotData}.
     *
     * @param spot
     *         The {@link ScheduleSpotData} from which the {@link ReservedSpot} will be created.
     * @param firstEpisode
     *         The first episode to use when creating the {@link ReservedSpot}.
     * @param <T>
     *         Type of the {@link WatchTarget}.
     *
     * @return A {@link ReservedSpot}.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <T extends WatchTarget> @NotNull ReservedSpot<T> of(@NotNull ScheduleSpotData<T> spot, int firstEpisode) {

        return new ReservedSpot<>(
                spot.getWatchTarget(),
                spot.getStartingAt(),
                firstEpisode,
                spot.getEpisodeCount(),
                spot.isSkipEnabled()
        );
    }

    /**
     * Delay the current {@link ReservedSpot} by the duration provided. This will create a new instance.
     *
     * @param duration
     *         The duration by which the {@link ReservedSpot} should be delayed.
     *
     * @return A new {@link ReservedSpot} with the start delayed by the amount provided.
     */
    @Contract(value = "_ -> new", pure = true)
    public ReservedSpot<T> delayOf(Duration duration) {

        return new ReservedSpot<>(
                this.watchTarget,
                this.startingAt.plus(duration),
                this.firstEpisode,
                this.episodeCount,
                this.skipEnabled
        );
    }

    /**
     * Retrieve the effective duration of this {@link ReservedSpot}.
     *
     * @return The duration of the {@link ReservedSpot}.
     */
    public @NotNull Duration duration() {

        if (this.episodeCount() == 1) return Duration.ofMinutes(this.watchTarget().getEpisodeDuration());

        int totalRuntime       = this.watchTarget().getEpisodeDuration() * this.episodeCount();
        int superfluousRuntime = this.skipEnabled() ? (this.episodeCount() - 1) * 3 : 0;

        return Duration.ofMinutes(totalRuntime - superfluousRuntime);
    }

}
