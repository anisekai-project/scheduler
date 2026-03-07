package fr.anisekai.scheduler.event.interfaces.entities;

import fr.anisekai.scheduler.event.interfaces.ScheduleSpotData;

/**
 * Represents an object that can be planned into a schedule and is associated with a specific range of episodes to
 * watch.
 * <p>
 * Extends {@link ScheduleSpotData} to inherit scheduling-related metadata, and adds episode-specific information such
 * as the first and last episode to be watched.
 * </p>
 *
 * @param <T>
 *         The type of {@link WatchTarget} associated with this planifiable item.
 */
public interface Planifiable<T extends WatchTarget> extends ScheduleSpotData<T> {

    /**
     * Retrieve the first episode number that will be watched in this {@link Planifiable}.
     *
     * @return The first episode number.
     */
    int getFirstEpisode();

    /**
     * Define the first episode number that will be watched in this {@link Planifiable}.
     *
     * @param firstEpisode
     *         The first episode number.
     */
    void setFirstEpisode(int firstEpisode);

    /**
     * Retrieve the last episode number that will be watched in this {@link Planifiable}. There is no setter counterpart
     * as this value is processed using {@link #getFirstEpisode()} and {@link #getEpisodeCount()}.
     *
     * @return The last episode number.
     */
    default int getLastEpisode() {

        return this.getFirstEpisode() + (this.getEpisodeCount() - 1);
    }

}
