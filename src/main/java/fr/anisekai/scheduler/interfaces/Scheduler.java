package fr.anisekai.scheduler.interfaces;

import fr.anisekai.scheduler.interfaces.entities.Planifiable;
import fr.anisekai.scheduler.interfaces.entities.WatchTarget;
import fr.anisekai.scheduler.plan.SchedulingPlan;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * Defines a generic scheduler capable of managing and orchestrating {@link Planifiable} entities over time for a given
 * {@link WatchTarget} type. A {@link  Scheduler} provides both querying and modification capabilities over a scheduled
 * state, including temporal navigation, validation, insertion, and recalibration of planned events.
 *
 * @param <T>
 *         The type of {@link WatchTarget} being scheduled.
 * @param <E>
 *         The final entity type being scheduled, extending {@code I}.
 * @param <ID>
 *         The type of the entity's identifier.
 *
 * @see Planifiable
 * @see ScheduleSpotData
 */
public interface Scheduler<T extends WatchTarget, E extends Planifiable<T>, ID extends Serializable> {

    // <editor-fold desc="State Queries">

    /**
     * Retrieve the current immutable state of scheduled entities managed by this {@link Scheduler}.
     *
     * @return A state
     */
    Set<E> getState();

    /**
     * Check in the current state for a {@link Planifiable} starting right before the provided {@link Instant}.
     *
     * @param when
     *         {@link Instant} filtering all {@link Planifiable} possible in the state.
     *
     * @return An optional {@link Planifiable}.
     */
    Optional<E> findPrevious(Instant when);

    /**
     * Check in the current state for a {@link Planifiable} starting right after the provided {@link Instant}.
     *
     * @param when
     *         {@link Instant} filtering all {@link Planifiable} possible in the state.
     *
     * @return An optional {@link Planifiable}.
     */
    Optional<E> findNext(Instant when);

    /**
     * Check in the current state for a {@link Planifiable} starting right before the provided {@link Instant} while
     * matching the provided {@link WatchTarget}.
     *
     * @param when
     *         {@link Instant} filtering all {@link Planifiable} possible in the state.
     * @param target
     *         {@link WatchTarget} further filtering possible {@link Planifiable}.
     *
     * @return An optional {@link Planifiable}.
     */
    Optional<E> findPrevious(Instant when, T target);

    /**
     * Check in the current state for a {@link Planifiable} starting right after the provided {@link Instant} while
     * matching the provided {@link WatchTarget}.
     *
     * @param when
     *         {@link Instant} filtering all planifiable possible in the state.
     * @param target
     *         {@link WatchTarget} further filtering possible {@link Planifiable}.
     *
     * @return An optional {@link Planifiable}.
     */
    Optional<E> findNext(Instant when, T target);

    /**
     * Check whether the given {@link ScheduleSpotData} can be scheduled without overlapping existing state or violating
     * constraints.
     *
     * @param spot
     *         {@link ScheduleSpotData} to validate for schedulability.
     *
     * @return True if the provided {@link ScheduleSpotData} can be scheduled, false otherwise.
     */
    boolean canSchedule(ScheduleSpotData<T> spot);

    // </editor-fold>

    // <editor-fold desc="State Actions">

    /**
     * Generates a plan to schedule the provided {@link ScheduleSpotData} within this {@link Scheduler}.
     *
     * @param spot
     *         {@link ScheduleSpotData} to use as source for scheduling data.
     *
     * @return The scheduling plan containing the necessary operations.
     */
    SchedulingPlan<ID> schedule(ScheduleSpotData<T> spot);

    /**
     * Generates a plan to delay every {@link Planifiable} being in the specified interval.
     *
     * @param from
     *         {@link Instant} defining the start of the interval
     * @param interval
     *         {@link Duration} defining the length of the interval
     * @param delay
     *         {@link Duration} defining the length of the delay to apply to every matching {@link Planifiable}.
     *
     * @return A scheduling plan containing the update operations.
     */
    SchedulingPlan<ID> delay(Instant from, Duration interval, Duration delay);

    /**
     * Generates a plan by reprocessing all scheduled entities to ensure consistency. This process may trim or adjust
     * entries that are misaligned or redundant.
     * <p>
     * Existing events will not be merged.
     *
     * @return A {@link SchedulingPlan} summarizing the number of updates and deletions to be performed.
     */
    SchedulingPlan<ID> calibrate();

    // </editor-fold>

}
