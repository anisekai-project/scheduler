package fr.anisekai.scheduler.event.data;

import fr.anisekai.scheduler.event.interfaces.ScheduleSpotData;
import fr.anisekai.scheduler.event.interfaces.entities.Planifiable;
import fr.anisekai.scheduler.event.interfaces.entities.WatchTarget;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Class representing a {@link Planifiable} that has not been commited yet.
 *
 * @param <T>
 *         Type of the {@link WatchTarget}.
 */
public class BookedPlanifiable<T extends WatchTarget> implements Planifiable<T> {

    private final T       target;
    private       Instant startingAt;
    private       int     firstEpisode;
    private       int     episodeCount;
    private       boolean skipEnabled;

    /**
     * Create a {@link BookedPlanifiable} based on the provided {@link ScheduleSpotData}.
     *
     * @param scheduleSpotData
     *         A {@link ScheduleSpotData}.
     */
    public BookedPlanifiable(ScheduleSpotData<T> scheduleSpotData) {

        this(scheduleSpotData, scheduleSpotData.getWatchTarget().getWatched() + 1);
    }

    /**
     * Create a {@link BookedPlanifiable} based on the provided {@link ScheduleSpotData} and first episode.
     *
     * @param scheduleSpotData
     *         A {@link ScheduleSpotData}.
     * @param firstEpisode
     *         The first episode number that would be broadcasted by this {@link BookedPlanifiable} once commited.
     */
    public BookedPlanifiable(ScheduleSpotData<T> scheduleSpotData, int firstEpisode) {

        this.target       = scheduleSpotData.getWatchTarget();
        this.startingAt   = scheduleSpotData.getStartingAt();
        this.firstEpisode = firstEpisode;
        this.episodeCount = scheduleSpotData.getEpisodeCount();
        this.skipEnabled  = scheduleSpotData.isSkipEnabled();
    }

    @Override
    public int getFirstEpisode() {

        return this.firstEpisode;
    }

    @Override
    public void setFirstEpisode(int firstEpisode) {

        this.firstEpisode = firstEpisode;
    }

    @Override
    public @NotNull T getWatchTarget() {

        return this.target;
    }

    @Override
    public void setWatchTarget(@NotNull T watchTarget) {

        throw new UnsupportedOperationException("You cannot change the WatchTarget of a BookedPlanifiable");
    }

    @Override
    public @NotNull Instant getStartingAt() {

        return this.startingAt;
    }

    @Override
    public void setStartingAt(@NotNull Instant time) {

        this.startingAt = time;
    }

    @Override
    public int getEpisodeCount() {

        return this.episodeCount;
    }

    @Override
    public void setEpisodeCount(int episodeCount) {

        this.episodeCount = episodeCount;
    }

    @Override
    public boolean isSkipEnabled() {

        return this.skipEnabled;
    }

    @Override
    public void setSkipEnabled(boolean skipEnabled) {

        this.skipEnabled = skipEnabled;
    }

}
