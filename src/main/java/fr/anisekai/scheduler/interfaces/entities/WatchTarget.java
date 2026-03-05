package fr.anisekai.scheduler.interfaces.entities;

/**
 * Represents a target object that can be watched, such as a show, movie, or any serialized media entity with episodes.
 *
 * <p>This interface defines the common contract for tracking the watch progress and metadata of a watchable item,
 * including:</p>
 * <ul>
 *   <li>The number of episodes already watched</li>
 *   <li>The total number of episodes (including possible estimations)</li>
 *   <li>The duration of each episode</li>
 * </ul>
 *
 * <p>Implementations are expected to manage state consistently, and may allow negative values for total episode count
 * to indicate estimates for ongoing series.</p>
 */
public interface WatchTarget {

    /**
     * Retrieve the number of episode watched for this {@link WatchTarget}.
     *
     * @return Number of episode watched.
     */
    int getWatched();

    /**
     * Define the number of episode watched for this {@link WatchTarget}.
     *
     * @param watched
     *         Number of episode watched.
     */
    void setWatched(int watched);

    /**
     * Retrieve the total amount of episode for this {@link WatchTarget}. Negative values will be used for a temporary
     * estimation of the total amount of episode.
     *
     * @return Number of episode in total
     */
    int getTotal();

    /**
     * Define the number of episode in total.
     *
     * @param total
     *         Number of episode in total
     */
    void setTotal(int total);

    /**
     * Retrieve the duration of one episode.
     *
     * @return Duration of one episode.
     */
    int getEpisodeDuration();

    /**
     * Define the duration of one episode.
     *
     * @param episodeDuration
     *         Duration of one episode.
     */
    void setEpisodeDuration(int episodeDuration);

}
