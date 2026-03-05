package fr.anisekai.scheduler;

import fr.anisekai.scheduler.data.BookedPlanifiable;
import fr.anisekai.scheduler.exceptions.DelayOverlapException;
import fr.anisekai.scheduler.exceptions.InvalidSchedulingDurationException;
import fr.anisekai.scheduler.exceptions.NotSchedulableException;
import fr.anisekai.scheduler.interfaces.ScheduleSpotData;
import fr.anisekai.scheduler.interfaces.Scheduler;
import fr.anisekai.scheduler.interfaces.entities.Planifiable;
import fr.anisekai.scheduler.interfaces.entities.WatchTarget;
import fr.anisekai.scheduler.plan.SchedulingAction;
import fr.anisekai.scheduler.plan.SchedulingPlan;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class allowing easy management of a schedule.
 *
 * @param <T>
 *         The watch target type. A watch target is some sort of container, representing a movie or series.
 * @param <E>
 *         The entity type. It is the type that will be scheduled.
 * @param <ID>
 *         The type of the entity's identifier.
 */
public class EventScheduler<T extends WatchTarget, E extends Planifiable<T>, ID extends Serializable> implements Scheduler<T, E, ID> {

    /**
     * If two {@link Planifiable} are of the following duration apart, they will be merged once scheduled. The duration
     * is done by comparing one {@link Planifiable#getStartingAt()} with the endpoint of another {@link Planifiable},
     * being {@link Planifiable#getStartingAt()} + {@link Planifiable#getDuration()}.
     */
    public static final Duration MERGE_MAGNET_LIMIT = Duration.ofMinutes(10);

    private final Set<E>          state;
    private final Function<E, ID> idExtractor;

    /**
     * Create a new instance of {@link Scheduler}.
     *
     * @param items
     *         Default collection of {@link Planifiable} that will populate the state.
     * @param idExtractor
     *         A function to extract the identifier from an entity.
     */
    public EventScheduler(Collection<E> items, Function<E, ID> idExtractor) {

        this.state       = new HashSet<>(items);
        this.idExtractor = idExtractor;
    }

    /**
     * Create a {@link Stream} of the current {@link EventScheduler} state, where every item will be filtered based on
     * the return value of {@link ScheduleSpotData#getStartingAt()}. If the returned value is before the provided
     * {@link Instant}, the item will be kept.
     *
     * @param when
     *         The {@link Instant} delimiting item filtering.
     *
     * @return A filtered {@link Stream} of the current state.
     */
    private Stream<E> findPreviousQuery(Instant when) {

        return this.getState().stream().filter(item -> item.getStartingAt().isBefore(when));
    }

    /**
     * Create a {@link Stream} of the current {@link EventScheduler} state, where every item will be filtered based on
     * the return value of {@link ScheduleSpotData#getStartingAt()}. If the returned value is after the provided
     * {@link Instant}, the item will be kept.
     *
     * @param when
     *         The {@link Instant} delimiting item filtering.
     *
     * @return A filtered {@link Stream} of the current state.
     */
    private Stream<E> findAfterQuery(Instant when) {

        return this.getState().stream().filter(item -> item.getStartingAt().isAfter(when));
    }

    @Override
    public Set<E> getState() {

        return Collections.unmodifiableSet(this.state);
    }

    @Override
    public Optional<E> findPrevious(Instant when) {

        return this.findPreviousQuery(when).max(Comparator.comparing(Planifiable::getStartingAt));
    }

    @Override
    public Optional<E> findNext(Instant when) {

        return this.findAfterQuery(when).min(Comparator.comparing(Planifiable::getStartingAt));
    }

    @Override
    public Optional<E> findPrevious(Instant when, T target) {

        return this.findPreviousQuery(when)
                   .filter(item -> item.getWatchTarget().equals(target))
                   .max(Comparator.comparing(Planifiable::getStartingAt));
    }

    @Override
    public Optional<E> findNext(Instant when, T target) {

        return this.findAfterQuery(when)
                   .filter(item -> item.getWatchTarget().equals(target))
                   .min(Comparator.comparing(Planifiable::getStartingAt));
    }

    @Override
    public boolean canSchedule(ScheduleSpotData<T> spot) {

        Duration duration = spot.getDuration();

        if (duration.isNegative() || duration.isZero()) {
            throw new InvalidSchedulingDurationException();
        }

        // Add one second to catch equals case
        boolean prevOverlap = this.findPrevious(spot.getStartingAt().plusSeconds(1))
                                  .map(item -> isOverlapping(spot, item))
                                  .orElse(false);

        // Remove one second to catch equals case
        boolean nextOverlap = this.findNext(spot.getStartingAt().minusSeconds(1))
                                  .map(item -> isOverlapping(spot, item))
                                  .orElse(false);

        return !prevOverlap && !nextOverlap;
    }

    @Override
    public SchedulingPlan<ID> schedule(ScheduleSpotData<T> spot) {

        if (!this.canSchedule(spot)) {
            throw new NotSchedulableException();
        }

        SchedulingPlan<ID> plan = new SchedulingPlan<>();

        Optional<E> optPrev       = this.findPrevious(spot.getStartingAt());
        Optional<E> optNext       = this.findNext(spot.getStartingAt());
        Optional<E> optTargetPrev = this.findPrevious(spot.getStartingAt(), spot.getWatchTarget());

        boolean isPrevCombinable = optPrev.map(item -> mayMerge(item, spot)).orElse(false);
        boolean isNextCombinable = optNext.map(item -> mayMerge(spot, item)).orElse(false);

        if (isPrevCombinable && isNextCombinable) { // Dual way merge
            E prev = optPrev.get();
            E next = optNext.get();

            int newCount = prev.getEpisodeCount() + spot.getEpisodeCount() + next.getEpisodeCount();

            plan.addAction(new SchedulingAction.UpdateAction<>(
                    this.idExtractor.apply(prev),
                    item -> item.setEpisodeCount(newCount)
            ));
            plan.addAction(new SchedulingAction.DeleteAction<>(this.idExtractor.apply(next)));
            return plan;
        }

        if (isPrevCombinable) {

            E   prev     = optPrev.get();
            int newCount = prev.getEpisodeCount() + spot.getEpisodeCount();

            plan.addAction(new SchedulingAction.UpdateAction<>(
                    this.idExtractor.apply(prev),
                    item -> item.setEpisodeCount(newCount)
            ));
            return plan;
        }

        if (isNextCombinable) {

            E   next     = optNext.get();
            int newCount = next.getEpisodeCount() + spot.getEpisodeCount();
            int firstEpisode = optTargetPrev
                    .map(item -> item.getFirstEpisode() + item.getEpisodeCount())
                    .orElseGet(() -> spot.getWatchTarget().getWatched() + 1);

            plan.addAction(new SchedulingAction.UpdateAction<>(
                    this.idExtractor.apply(next),
                    item -> {
                        item.setFirstEpisode(firstEpisode);
                        item.setEpisodeCount(newCount);
                        item.setStartingAt(spot.getStartingAt());
                    }
            ));
            return plan;
        }


        Planifiable<T> planifiable;
        if (optTargetPrev.isPresent()) {
            E prev = optTargetPrev.get();
            planifiable = new BookedPlanifiable<>(spot, prev.getFirstEpisode() + prev.getEpisodeCount());
        } else {
            planifiable = new BookedPlanifiable<>(spot);
        }

        plan.addAction(new SchedulingAction.CreateAction(planifiable));
        return plan;
    }

    @Override
    public SchedulingPlan<ID> delay(Instant from, Duration interval, Duration delay) {

        Instant to = from.plus(interval);

        List<E> events = this.getState()
                             .stream()
                             .filter(item -> DateTimeUtils.isAfterOrEquals(item.getStartingAt(), from))
                             .filter(item -> DateTimeUtils.isBeforeOrEquals(item.getEndingAt(), to))
                             .toList();

        // Creating a temporary state excluding events to delay to check for overlaps
        List<E> temporaryState = this.getState().stream().filter(item -> !events.contains(item)).toList();

        // Check if nothing overlaps the temporary state with the delay
        if (events
                .stream()
                .map(item -> (Planifiable<T>) new BookedPlanifiable<>(item))
                .peek(item -> item.setStartingAt(item.getStartingAt().plus(delay)))
                .anyMatch(item -> temporaryState.stream().anyMatch(state -> isOverlapping(item, state)))
        ) {

            throw new DelayOverlapException("One of the event cannot be delayed without conflict.");
        }

        SchedulingPlan<ID> plan = new SchedulingPlan<>();
        events.forEach(event -> plan.addAction(new SchedulingAction.UpdateAction<>(
                this.idExtractor.apply(event),
                item -> item.setStartingAt(item.getStartingAt().plus(delay))
        )));
        return plan;
    }

    @Override
    public SchedulingPlan<ID> calibrate() {

        SchedulingPlan<ID> plan = new SchedulingPlan<>();

        // Store the max possible episode for each target
        Map<T, Integer> targetMaxEpisode = this
                .getState()
                .stream()
                .map(ScheduleSpotData::getWatchTarget)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), WatchTarget::getTotal));

        // Store the progress for each target
        Map<T, Integer> targetProgression = this
                .getState()
                .stream()
                .map(ScheduleSpotData::getWatchTarget)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), WatchTarget::getWatched));

        List<E> sorted = this.getState()
                             .stream()
                             .sorted(Comparator.comparing(ScheduleSpotData::getStartingAt))
                             .toList();

        for (E event : sorted) {

            int maxEpisode  = targetMaxEpisode.get(event.getWatchTarget());
            int progression = targetProgression.get(event.getWatchTarget());

            if (maxEpisode < 0) { // Support for "estimate" amount of episode, which are represented by negative number.
                maxEpisode = maxEpisode * -1;
            }

            boolean correctFirstEpisode = event.getFirstEpisode() == progression + 1;
            boolean correctEpisodeCount = (event.getFirstEpisode() + event.getEpisodeCount()) - 1 <= maxEpisode;

            int fixedFirstEpisode = progression + 1;
            int fixedEpisodeCount = Math.min(maxEpisode - progression, event.getEpisodeCount());

            // Don't keep overflowing events
            if (fixedFirstEpisode > maxEpisode) {
                plan.addAction(new SchedulingAction.DeleteAction<>(this.idExtractor.apply(event)));
                continue;
            }

            // If we require at least one thing to be updated, start the update
            if (!correctEpisodeCount || !correctFirstEpisode) {
                plan.addAction(new SchedulingAction.UpdateAction<>(
                        this.idExtractor.apply(event), item -> {
                    item.setFirstEpisode(fixedFirstEpisode);
                    item.setEpisodeCount(fixedEpisodeCount);
                }
                ));
            }

            // Keep track of our movement throughout the schedule
            targetProgression.put(event.getWatchTarget(), fixedFirstEpisode + fixedEpisodeCount - 1);
        }

        return plan;
    }

    /**
     * Check if the two provided {@link ScheduleSpotData} can be merged. This is where the rule of merging should be
     * decided (timing, content, etc...)
     *
     * @param element
     *         The first {@link ScheduleSpotData}
     * @param planifiable
     *         The second {@link ScheduleSpotData}
     *
     * @return True if both event can be merged, false otherwise.
     */
    private static <T extends WatchTarget> boolean mayMerge(ScheduleSpotData<T> element, ScheduleSpotData<T> planifiable) {

        long breakTime  = Duration.between(element.getEndingAt(), planifiable.getStartingAt()).toSeconds();
        long magnetTime = MERGE_MAGNET_LIMIT.toSeconds();

        boolean isWithinMagnetTime = breakTime <= magnetTime;
        boolean isSameGroup        = Objects.equals(element.getWatchTarget(), planifiable.getWatchTarget());

        return isWithinMagnetTime && isSameGroup;
    }

    /**
     * Check if provided {@link ScheduleSpotData} overlap one another.
     *
     * @param one
     *         The first {@link ScheduleSpotData}
     * @param two
     *         The second {@link ScheduleSpotData}
     *
     * @return True if the {@link ScheduleSpotData} overlaps, false otherwise.
     */
    private static <T extends WatchTarget> boolean isOverlapping(ScheduleSpotData<T> one, ScheduleSpotData<T> two) {

        Instant startingAt = one.getStartingAt();
        Instant endingAt   = startingAt.plus(one.getDuration());

        Instant itemStartingAt = two.getStartingAt();
        Instant itemEndingAt   = itemStartingAt.plus(two.getDuration());

        return !startingAt.isAfter(itemEndingAt) && !startingAt.equals(itemEndingAt) && !endingAt.isBefore(
                itemStartingAt) && !endingAt.equals(itemStartingAt);
    }

}
