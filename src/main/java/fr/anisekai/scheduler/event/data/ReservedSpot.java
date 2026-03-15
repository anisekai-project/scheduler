package fr.anisekai.scheduler.event.data;

import fr.anisekai.scheduler.event.interfaces.ScheduleSpotData;
import fr.anisekai.scheduler.event.interfaces.entities.WatchTarget;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

public record ReservedSpot<T extends WatchTarget>(
        @NotNull T watchTarget,
        @NotNull Instant startingAt,
        int firstEpisode,
        int episodeCount,
        boolean skipEnabled
) {

    @Contract(value = "_ -> new", pure = true)
    public static <T extends WatchTarget> @NotNull ReservedSpot<T> of(@NotNull ScheduleSpotData<T> spot) {

        return of(spot, spot.getWatchTarget().getWatched() + 1);
    }

    public static <T extends WatchTarget> @NotNull ReservedSpot<T> of(@NotNull ScheduleSpotData<T> spot, int firstEpisode) {

        return new ReservedSpot<>(
                spot.getWatchTarget(),
                spot.getStartingAt(),
                firstEpisode,
                spot.getEpisodeCount(),
                spot.isSkipEnabled()
        );
    }

    public ReservedSpot<T> delayOf(Duration duration) {

        return new ReservedSpot<>(
                this.watchTarget,
                this.startingAt.plus(duration),
                this.firstEpisode,
                this.episodeCount,
                this.skipEnabled
        );
    }

    public @NotNull Duration duration() {

        if (this.episodeCount() == 1) return Duration.ofMinutes(this.watchTarget().getEpisodeDuration());

        int totalRuntime       = this.watchTarget().getEpisodeDuration() * this.episodeCount();
        int superfluousRuntime = this.skipEnabled() ? (this.episodeCount() - 1) * 3 : 0;

        return Duration.ofMinutes(totalRuntime - superfluousRuntime);
    }

}
