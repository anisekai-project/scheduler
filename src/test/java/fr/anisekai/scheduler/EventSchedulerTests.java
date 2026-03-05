package fr.anisekai.scheduler;

import fr.anisekai.scheduler.exceptions.DelayOverlapException;
import fr.anisekai.scheduler.exceptions.InvalidSchedulingDurationException;
import fr.anisekai.scheduler.exceptions.NotSchedulableException;
import fr.anisekai.scheduler.interfaces.ScheduleSpotData;
import fr.anisekai.scheduler.interfaces.Scheduler;
import fr.anisekai.scheduler.interfaces.entities.Planifiable;
import fr.anisekai.scheduler.interfaces.entities.WatchTarget;
import fr.anisekai.scheduler.plan.SchedulingAction;
import fr.anisekai.scheduler.plan.SchedulingPlan;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventScheduler")
@Tags({@Tag("unit-test"), @Tag("event-scheduler")})
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class EventSchedulerTests {

    //region Test Data
    static class TestWatchTarget implements WatchTarget {

        private int watched;
        private int total;
        private int episodeDuration;

        public TestWatchTarget(int watched, int total, int episodeDuration) {

            this.watched         = watched;
            this.total           = total;
            this.episodeDuration = episodeDuration;
        }

        @Override
        public int getWatched() {return this.watched;}

        @Override
        public void setWatched(int watched) {this.watched = watched;}

        @Override
        public int getTotal() {return this.total;}

        @Override
        public void setTotal(int total) {this.total = total;}

        @Override
        public int getEpisodeDuration() {return this.episodeDuration;}

        @Override
        public void setEpisodeDuration(int episodeDuration) {this.episodeDuration = episodeDuration;}

    }

    static class TestWatchParty implements Planifiable<TestWatchTarget> {

        private static final AtomicInteger   ID_COUNTER = new AtomicInteger(0);
        private final        Integer         id;
        private              TestWatchTarget watchTarget;
        private              Instant         startingAt;
        private              int             episodeCount;
        private              int             firstEpisode;
        private              boolean         skipEnabled;

        public TestWatchParty(TestWatchTarget watchTarget, Instant startingAt, int episodeCount, int firstEpisode) {

            this.id           = ID_COUNTER.incrementAndGet();
            this.watchTarget  = watchTarget;
            this.startingAt   = startingAt;
            this.episodeCount = episodeCount;
            this.firstEpisode = firstEpisode;
            this.skipEnabled  = true;
        }

        public Integer getId() {return this.id;}

        @NotNull
        @Override
        public TestWatchTarget getWatchTarget() {return this.watchTarget;}

        @Override
        public void setWatchTarget(@NotNull TestWatchTarget watchTarget) {this.watchTarget = watchTarget;}

        @NotNull
        @Override
        public Instant getStartingAt() {return this.startingAt;}

        @Override
        public void setStartingAt(@NotNull Instant time) {this.startingAt = time;}

        @Override
        public int getEpisodeCount() {return this.episodeCount;}

        @Override
        public void setEpisodeCount(int episodeCount) {this.episodeCount = episodeCount;}

        @Override
        public boolean isSkipEnabled() {return this.skipEnabled;}

        @Override
        public void setSkipEnabled(boolean skipEnabled) {this.skipEnabled = skipEnabled;}

        @Override
        public int getFirstEpisode() {return this.firstEpisode;}

        @Override
        public void setFirstEpisode(int firstEpisode) {this.firstEpisode = firstEpisode;}

    }

    static class TestSpot implements ScheduleSpotData<TestWatchTarget> {

        private TestWatchTarget watchTarget;
        private Instant         startingAt;
        private int             episodeCount;

        public TestSpot(TestWatchTarget watchTarget, Instant startingAt, int episodeCount) {

            this.watchTarget  = watchTarget;
            this.startingAt   = startingAt;
            this.episodeCount = episodeCount;
        }

        @NotNull
        @Override
        public TestWatchTarget getWatchTarget() {return this.watchTarget;}

        @Override
        public void setWatchTarget(@NotNull TestWatchTarget watchTarget) {this.watchTarget = watchTarget;}

        @NotNull
        @Override
        public Instant getStartingAt() {return this.startingAt;}

        @Override
        public void setStartingAt(@NotNull Instant time) {this.startingAt = time;}

        @Override
        public int getEpisodeCount() {return this.episodeCount;}

        @Override
        public void setEpisodeCount(int episodeCount) {this.episodeCount = episodeCount;}

        @Override
        public boolean isSkipEnabled() {return true;}

        @Override
        public void setSkipEnabled(boolean skipEnabled) {}

    }

    static class TestData {

        public static final Instant BASE_DATETIME = Instant.now().plus(30, ChronoUnit.DAYS);

        public final TestWatchTarget target1;
        public final TestWatchTarget target2;
        public final TestWatchParty  partyA1;
        public final TestWatchParty  partyB1;
        public final TestWatchParty  partyB2;

        public TestData() {

            this.target1 = new TestWatchTarget(0, 12, 24);
            this.target2 = new TestWatchTarget(0, 24, 24);

            this.partyA1 = new TestWatchParty(this.target1, BASE_DATETIME, 2, 1);
            this.partyB1 = new TestWatchParty(this.target1, BASE_DATETIME.plus(2, ChronoUnit.HOURS), 2, 3);
            this.partyB2 = new TestWatchParty(this.target1, BASE_DATETIME.plus(4, ChronoUnit.HOURS), 2, 5);
        }

        public List<TestWatchParty> dataBank() {

            return new ArrayList<>(List.of(this.partyA1, this.partyB1, this.partyB2));
        }

    }
    //endregion

    private Scheduler<TestWatchTarget, TestWatchParty, Integer> scheduler;
    private TestData                                            data;

    @BeforeEach
    public void setup() {

        this.data      = new TestData();
        this.scheduler = new EventScheduler<>(this.data.dataBank(), TestWatchParty::getId);
    }

    private <T extends SchedulingAction, ID extends Serializable> T getSingleAction(SchedulingPlan<ID> plan, Class<T> actionClass) {

        assertEquals(1, plan.getActions().size(), "Expected a single action in the plan.");
        SchedulingAction action = plan.getActions().getFirst();
        assertInstanceOf(actionClass, action, "Action is not of the expected type.");
        return actionClass.cast(action);
    }

    private <ID extends Serializable> SchedulingAction.CreateAction getCreateAction(SchedulingPlan<ID> plan) {

        return this.getSingleAction(plan, SchedulingAction.CreateAction.class);
    }

    @SuppressWarnings("unchecked")
    private <ID extends Serializable> SchedulingAction.UpdateAction<ID> getUpdateAction(SchedulingPlan<ID> plan) {

        return this.getSingleAction(plan, SchedulingAction.UpdateAction.class);
    }

    @SuppressWarnings("unchecked")
    private <ID extends Serializable> SchedulingAction.DeleteAction<ID> getDeleteAction(SchedulingPlan<ID> plan) {

        return this.getSingleAction(plan, SchedulingAction.DeleteAction.class);
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling - No Conflicts")
    public void testSingleSchedulingNoConflict() {

        Instant                           scheduleAt = TestData.BASE_DATETIME.plus(1, ChronoUnit.DAYS);
        ScheduleSpotData<TestWatchTarget> spot       = new TestSpot(this.data.target2, scheduleAt, 1);

        assertTrue(this.scheduler.canSchedule(spot), "The event can't be scheduled.");

        SchedulingPlan<Integer> plan = assertDoesNotThrow(
                () -> this.scheduler.schedule(spot),
                "An error occurred while scheduling the event."
        );

        SchedulingAction.CreateAction action = this.getCreateAction(plan);
        Planifiable<?>                data   = action.data();

        assertEquals(1, data.getFirstEpisode(), "First episode mismatch.");
        assertEquals(1, data.getEpisodeCount(), "Episode count mismatch.");
        assertEquals(scheduleAt, data.getStartingAt(), "Starting datetime mismatch.");
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling - With Conflicts")
    public void testSingleSchedulingWithConflict() {

        ScheduleSpotData<TestWatchTarget> spot = new TestSpot(this.data.target2, TestData.BASE_DATETIME, 1);
        assertFalse(this.scheduler.canSchedule(spot), "The event can be scheduled.");
        assertThrows(NotSchedulableException.class, () -> this.scheduler.schedule(spot));
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling - Follow-up")
    public void testSingleSchedulingFollowUp() {

        Instant                           scheduleAt = TestData.BASE_DATETIME.plus(1, ChronoUnit.DAYS);
        ScheduleSpotData<TestWatchTarget> spot       = new TestSpot(this.data.target1, scheduleAt, 1);

        assertTrue(this.scheduler.canSchedule(spot), "The event can't be scheduled.");

        SchedulingPlan<Integer> plan = assertDoesNotThrow(() -> this.scheduler.schedule(spot));

        SchedulingAction.CreateAction action = this.getCreateAction(plan);
        Planifiable<?>                data   = action.data();

        assertEquals(
                7,
                data.getFirstEpisode(),
                "First episode should be calculated after all existing events for the target."
        );
        assertEquals(1, data.getEpisodeCount(), "Episode count mismatch.");
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling | Merging - To Left")
    public void testSingleSchedulingMergingToLeft() {

        ScheduleSpotData<TestWatchTarget> spot = new TestSpot(
                this.data.target1,
                this.data.partyB2.getEndingAt().plus(5, ChronoUnit.MINUTES),
                1
        );

        assertTrue(this.scheduler.canSchedule(spot), "The event can't be scheduled.");

        SchedulingPlan<Integer> plan = this.scheduler.schedule(spot);

        SchedulingAction.UpdateAction<Integer> action = this.getUpdateAction(plan);
        assertEquals(this.data.partyB2.getId(), action.targetId(), "The plan should target the left party for update.");

        TestWatchParty mockParty = new TestWatchParty(
                this.data.target1,
                this.data.partyB2.getStartingAt(), this.data.partyB2.getEpisodeCount(),
                this.data.partyB2.getFirstEpisode()
        );

        action.updateHook().accept(mockParty);

        assertEquals(3, mockParty.getEpisodeCount(), "Episode count should be merged.");
        assertEquals(
                this.data.partyB2.getStartingAt(),
                mockParty.getStartingAt(),
                "Starting time should not change on a left merge."
        );
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling | Merging - To Right")
    public void testSingleSchedulingMergingToRight() {

        ScheduleSpotData<TestWatchTarget> spot = new TestSpot(
                this.data.target1,
                this.data.partyB1.getStartingAt()
                                 .minus(Duration.ofMinutes(24))
                                 .minus(5, ChronoUnit.MINUTES),
                1
        );
        Instant scheduleAt = spot.getStartingAt();

        assertTrue(this.scheduler.canSchedule(spot), "The event can't be scheduled.");

        SchedulingPlan<Integer> plan = this.scheduler.schedule(spot);

        SchedulingAction.UpdateAction<Integer> action = this.getUpdateAction(plan);
        assertEquals(
                this.data.partyB1.getId(),
                action.targetId(),
                "The plan should target the right party for update."
        );

        TestWatchParty mockParty = new TestWatchParty(
                this.data.target1,
                this.data.partyB1.getStartingAt(), this.data.partyB1.getEpisodeCount(),
                this.data.partyB1.getFirstEpisode()
        );
        action.updateHook().accept(mockParty);

        assertEquals(3, mockParty.getEpisodeCount(), "Episode count should be merged.");
        assertEquals(3, mockParty.getFirstEpisode(), "First episode should be correct based on previous events.");
        assertEquals(scheduleAt, mockParty.getStartingAt(), "Starting time should be updated to the new spot's time.");
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling | Merging - Left & Right (Sandwich Merging)")
    public void testSingleSchedulingSandwichMerging() {

        ScheduleSpotData<TestWatchTarget> spot = new TestSpot(
                this.data.target1,
                this.data.partyB1.getEndingAt().plus(5, ChronoUnit.MINUTES),
                1
        );
        Instant scheduleAt = spot.getStartingAt();

        this.data.partyB2.setStartingAt(scheduleAt.plus(spot.getDuration()).plus(5, ChronoUnit.MINUTES));

        assertTrue(this.scheduler.canSchedule(spot), "The event can't be scheduled.");

        SchedulingPlan<Integer> plan = this.scheduler.schedule(spot);
        assertEquals(2, plan.getActions().size(), "Expected one update and one delete action.");

        SchedulingAction.UpdateAction<Integer> updateAction = plan
                .getActions()
                .stream()
                .filter(SchedulingAction.UpdateAction.class::isInstance)
                .map(a -> (SchedulingAction.UpdateAction<Integer>) a)
                .findFirst()
                .orElseThrow();

        SchedulingAction.DeleteAction<Integer> deleteAction = plan
                .getActions()
                .stream()
                .filter(SchedulingAction.DeleteAction.class::isInstance)
                .map(a -> (SchedulingAction.DeleteAction<Integer>) a)
                .findFirst()
                .orElseThrow();

        assertEquals(this.data.partyB1.getId(), updateAction.targetId(), "The update should target the first party.");
        assertEquals(this.data.partyB2.getId(), deleteAction.targetId(), "The delete should target the second party.");

        Planifiable<TestWatchTarget> mockParty = new TestWatchParty(
                this.data.target1,
                this.data.partyB1.getStartingAt(), this.data.partyB1.getEpisodeCount(),
                this.data.partyB1.getFirstEpisode()
        );
        updateAction.updateHook().accept(mockParty);

        assertEquals(5, mockParty.getEpisodeCount(), "Episode count should be the sum of all three spots (2 + 1 + 2).");
        assertEquals(
                this.data.partyB1.getStartingAt(),
                mockParty.getStartingAt(),
                "Starting time should be from the first party."
        );
    }

    @Test
    @DisplayName("Scheduler | Delaying - Success")
    public void testDelayingSuccess() {

        Duration delay = Duration.ofHours(1);
        SchedulingPlan<Integer> plan = assertDoesNotThrow(() -> this.scheduler.delay(
                TestData.BASE_DATETIME,
                Duration.ofMinutes(60),
                delay
        ));

        SchedulingAction.UpdateAction<Integer> action = this.getUpdateAction(plan);
        assertEquals(this.data.partyA1.getId(), action.targetId(), "Wrong item targeted for delay.");

        Planifiable<TestWatchTarget> mockParty = new TestWatchParty(
                this.data.target1,
                this.data.partyA1.getStartingAt(),
                0,
                0
        );
        action.updateHook().accept(mockParty);

        assertEquals(
                this.data.partyA1.getStartingAt().plus(delay),
                mockParty.getStartingAt(),
                "Delay duration not respected."
        );
    }

    @Test
    @DisplayName("Scheduler | Delaying - Conflict")
    public void testDelayConflict() {

        assertThrows(
                DelayOverlapException.class, () -> this.scheduler.delay(
                        TestData.BASE_DATETIME,
                        Duration.ofMinutes(60),
                        Duration.between(this.data.partyA1.getStartingAt(), this.data.partyB1.getStartingAt())
                )
        );
    }

    @Test
    @DisplayName("Scheduler | Calibration - Raw")
    public void testCalibrationUpstreamRaw() {

        SchedulingPlan<Integer> plan = assertDoesNotThrow(() -> this.scheduler.calibrate());
        assertTrue(plan.getActions().isEmpty(), "Plan should be empty when no calibration is needed.");
    }

    @Test
    @DisplayName("Scheduler | Calibration - Upstream @ Update Only")
    public void testCalibrationUpstreamUpdateOnly() {

        this.data.target1.setWatched(1);
        SchedulingPlan<Integer> plan = assertDoesNotThrow(() -> this.scheduler.calibrate());

        assertEquals(3, plan.getActions().size(), "Expected 3 update actions.");
        assertTrue(
                plan.getActions().stream().allMatch(SchedulingAction.UpdateAction.class::isInstance),
                "All actions should be updates."
        );

        SchedulingAction.UpdateAction<Integer> actionForA1 = plan
                .getActions()
                .stream()
                .map(a -> (SchedulingAction.UpdateAction<Integer>) a)
                .filter(a -> a.targetId().equals(this.data.partyA1.getId()))
                .findFirst()
                .orElseThrow();

        Planifiable<TestWatchTarget> mockParty = new TestWatchParty(null, null, 0, this.data.partyA1.getFirstEpisode());
        actionForA1.updateHook().accept(mockParty);

        assertEquals(2, mockParty.getFirstEpisode(), "First episode for partyA1 should be recalibrated to 2.");
    }

    @Test
    @DisplayName("Scheduler | Calibration - Upstream @ Delete Only")
    public void testCalibrationUpstreamDeleteOnly() {

        this.data.target1.setWatched(12);
        SchedulingPlan<Integer> plan = assertDoesNotThrow(() -> this.scheduler.calibrate());

        assertEquals(3, plan.getActions().size(), "Expected 3 delete actions.");
        assertTrue(
                plan.getActions().stream().allMatch(SchedulingAction.DeleteAction.class::isInstance),
                "All actions should be deletes."
        );
    }

    @Test
    @DisplayName("Scheduler | Calibration - Upstream @ Update + Delete")
    public void testCalibrationUpstreamUpdateDelete() {

        this.data.target1.setWatched(8);
        SchedulingPlan<Integer> plan = assertDoesNotThrow(() -> this.scheduler.calibrate());

        assertEquals(3, plan.getActions().size(), "Expected 1 delete and 2 update actions.");

        long deleteCount = plan.getActions().stream().filter(SchedulingAction.DeleteAction.class::isInstance).count();
        long updateCount = plan.getActions().stream().filter(SchedulingAction.UpdateAction.class::isInstance).count();
        assertEquals(1, deleteCount, "Expected one party to be deleted.");
        assertEquals(2, updateCount, "Expected two parties to be updated.");

        SchedulingAction.DeleteAction<Integer> deleteAction = plan
                .getActions().stream()
                .filter(SchedulingAction.DeleteAction.class::isInstance)
                .map(a -> (SchedulingAction.DeleteAction<Integer>) a)
                .findFirst().orElseThrow();

        assertEquals(this.data.partyB2.getId(), deleteAction.targetId(), "partyB2 should be deleted.");
    }

    @Test
    @DisplayName("Scheduler | Calibration - Upstream @ Update + Delete + Shrink")
    public void testCalibrationUpstreamUpdateDeleteShrink() {

        this.data.target1.setWatched(2);
        this.data.target1.setTotal(3);

        SchedulingPlan<Integer> plan = assertDoesNotThrow(() -> this.scheduler.calibrate());

        long deleteCount = plan.getActions().stream().filter(SchedulingAction.DeleteAction.class::isInstance).count();
        long updateCount = plan.getActions().stream().filter(SchedulingAction.UpdateAction.class::isInstance).count();

        assertEquals(2, deleteCount, "partyB1 and partyB2 should be deleted as they are out of bounds.");
        assertEquals(1, updateCount, "partyA1 should be updated (shrunk).");

        SchedulingAction.UpdateAction<Integer> updateAction = plan
                .getActions()
                .stream()
                .filter(SchedulingAction.UpdateAction.class::isInstance)
                .findFirst()
                .map(action -> (SchedulingAction.UpdateAction<Integer>) action)
                .orElseThrow();

        TestWatchParty mockParty = new TestWatchParty(
                this.data.target1,
                this.data.partyA1.getStartingAt(), this.data.partyA1.getEpisodeCount(),
                this.data.partyA1.getFirstEpisode()
        );
        updateAction.updateHook().accept(mockParty);

        assertEquals(3, mockParty.getFirstEpisode(), "First episode should be updated to 3.");
        assertEquals(1, mockParty.getEpisodeCount(), "Episode count should be shrunk to 1 to fit total.");
    }

    @Test
    @DisplayName("Scheduler | Additional | Invalid Duration")
    void testInvalidDuration() {

        ScheduleSpotData<TestWatchTarget> spot = new TestSpot(this.data.target2, TestData.BASE_DATETIME, -1);
        assertThrows(InvalidSchedulingDurationException.class, () -> this.scheduler.canSchedule(spot));
    }

    @Test
    @DisplayName("Scheduler | Additional | Merge At Exact Magnet Limit")
    void testMergeAtBoundary() {

        Instant scheduleAt = this.data.partyA1.getEndingAt()
                                              .plus(EventScheduler.MERGE_MAGNET_LIMIT);
        ScheduleSpotData<TestWatchTarget> spot = new TestSpot(this.data.target1, scheduleAt, 1);

        assertTrue(this.scheduler.canSchedule(spot), "Should be able to schedule at the exact merge boundary.");
        SchedulingPlan<Integer> plan = this.scheduler.schedule(spot);

        this.getUpdateAction(plan);
    }

    @Test
    @DisplayName("Scheduler | Additional | No Merge Outside Magnet Limit")
    void testNoMergeOutsideBoundary() {

        Instant scheduleAt = this.data.partyA1.getEndingAt()
                                              .plus(EventScheduler.MERGE_MAGNET_LIMIT)
                                              .plusSeconds(1);
        ScheduleSpotData<TestWatchTarget> spot = new TestSpot(this.data.target1, scheduleAt, 1);

        assertTrue(this.scheduler.canSchedule(spot), "Should be able to schedule just outside the merge boundary.");
        SchedulingPlan<Integer> plan = this.scheduler.schedule(spot);

        this.getCreateAction(plan);
    }

    @Test
    @DisplayName("Scheduler | Additional | Calibration with Negative (Estimated) Total")
    void testCalibrationWithEstimatedTotal() {

        this.data.target1.setTotal(-12);
        this.data.target1.setWatched(2);

        SchedulingPlan<Integer> plan = assertDoesNotThrow(() -> this.scheduler.calibrate());

        long deleteCount = plan.getActions().stream().filter(SchedulingAction.DeleteAction.class::isInstance).count();
        long updateCount = plan.getActions().stream().filter(SchedulingAction.UpdateAction.class::isInstance).count();

        assertEquals(0, deleteCount, "No parties should be deleted.");
        assertEquals(3, updateCount, "All parties should be updated to reflect new watched count.");

        SchedulingAction.UpdateAction<Integer> actionForB2 = plan
                .getActions()
                .stream()
                .map(a -> (SchedulingAction.UpdateAction<Integer>) a)
                .filter(a -> a.targetId().equals(this.data.partyB2.getId()))
                .findFirst()
                .orElseThrow();

        Planifiable<TestWatchTarget> mockParty = new TestWatchParty(null, null, 0, 0);
        actionForB2.updateHook().accept(mockParty);

        assertEquals(7, mockParty.getFirstEpisode(), "partyB2 should start at episode 7 (5+2).");
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling | Merging - To Right without previous target")
    public void testSingleSchedulingMergingToRight_NoPreviousTarget() {

        this.scheduler = new EventScheduler<>(List.of(this.data.partyB1), TestWatchParty::getId);

        ScheduleSpotData<TestWatchTarget> spot = new TestSpot(
                this.data.target1,
                this.data.partyB1.getStartingAt()
                                 .minus(Duration.ofMinutes(24))
                                 .minus(5, ChronoUnit.MINUTES),
                1
        );
        Instant scheduleAt = spot.getStartingAt();

        assertTrue(this.scheduler.canSchedule(spot), "The event can't be scheduled.");

        SchedulingPlan<Integer> plan = this.scheduler.schedule(spot);

        SchedulingAction.UpdateAction<Integer> action = this.getUpdateAction(plan);
        assertEquals(
                this.data.partyB1.getId(),
                action.targetId(),
                "The plan should target the right party for update."
        );

        TestWatchParty mockParty = new TestWatchParty(
                this.data.target1,
                this.data.partyB1.getStartingAt(),
                this.data.partyB1.getEpisodeCount(),
                this.data.partyB1.getFirstEpisode()
        );
        action.updateHook().accept(mockParty);

        assertEquals(3, mockParty.getEpisodeCount(), "Episode count should be merged.");

        assertEquals(
                1,
                mockParty.getFirstEpisode(),
                "First episode should be calculated from the WatchTarget's progress."
        );
        assertEquals(scheduleAt, mockParty.getStartingAt(), "Starting time should be updated to the new spot's time.");
    }

    @Test
    @DisplayName("Scheduler | Find Operations | findNext - Success")
    void testFindNext_Success() {

        Optional<TestWatchParty> queryResult = this.scheduler.findNext(this.data.partyA1.getStartingAt());
        assertTrue(queryResult.isPresent(), "Should find the next event.");
        assertEquals(this.data.partyB1.getId(), queryResult.get().getId(), "Should return partyB1 as the next event.");
    }

    @Test
    @DisplayName("Scheduler | Find Operations | findNext - Empty")
    void testFindNext_Empty() {

        Optional<TestWatchParty> queryResult = this.scheduler.findNext(this.data.partyB2.getStartingAt());
        assertTrue(queryResult.isEmpty(), "Should not find any event after the last one.");
    }

    @Test
    @DisplayName("Scheduler | Find Operations | findPrevious - Success")
    void testFindPrevious_Success() {

        Optional<TestWatchParty> queryResult = this.scheduler.findPrevious(this.data.partyB2.getStartingAt());

        assertTrue(queryResult.isPresent(), "Should find the previous event.");
        assertEquals(
                this.data.partyB1.getId(),
                queryResult.get().getId(),
                "Should return partyB1 as the previous event."
        );
    }

    @Test
    @DisplayName("Scheduler | Find Operations | findPrevious - Empty")
    void testFindPrevious_Empty() {

        Optional<TestWatchParty> queryResult = this.scheduler.findPrevious(this.data.partyA1.getStartingAt());
        assertTrue(queryResult.isEmpty(), "Should not find any event before the first one.");
    }

    @Test
    @DisplayName("Scheduler | Find Operations | findNext - With Target Filter")
    void testFindNext_WithTarget() {
        // Add an event for another target
        TestWatchParty partyC = new TestWatchParty(
                this.data.target2,
                TestData.BASE_DATETIME.plus(1, ChronoUnit.HOURS),
                1,
                1
        );
        this.scheduler = new EventScheduler<>(
                List.of(this.data.partyA1, partyC, this.data.partyB1),
                TestWatchParty::getId
        );

        var nextForTarget1 = this.scheduler.findNext(this.data.partyA1.getStartingAt(), this.data.target1);
        assertTrue(nextForTarget1.isPresent(), "Should find next for target1.");
        assertEquals(this.data.partyB1.getId(), nextForTarget1.get().getId(), "Next for target1 should be partyB1.");

        var nextForTarget2 = this.scheduler.findNext(this.data.partyA1.getStartingAt(), this.data.target2);
        assertTrue(nextForTarget2.isPresent(), "Should find next for target2.");
        assertEquals(partyC.getId(), nextForTarget2.get().getId(), "Next for target2 should be partyC.");
    }

}
