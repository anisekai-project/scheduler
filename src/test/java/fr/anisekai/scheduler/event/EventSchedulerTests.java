package fr.anisekai.scheduler.event;

import fr.anisekai.scheduler.event.data.TestSpot;
import fr.anisekai.scheduler.event.data.TestWatchParty;
import fr.anisekai.scheduler.event.data.TestWatchTarget;
import fr.anisekai.scheduler.event.exceptions.DelayOverlapException;
import fr.anisekai.scheduler.event.exceptions.InvalidSchedulingDurationException;
import fr.anisekai.scheduler.event.exceptions.NotSchedulableException;
import fr.anisekai.scheduler.event.interfaces.ScheduleSpotData;
import fr.anisekai.scheduler.event.interfaces.Scheduler;
import fr.anisekai.scheduler.event.interfaces.entities.Planifiable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.anisekai.scheduler.ActionPlanAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Event Scheduler")
@Tags({@Tag("unit-test"), @Tag("event-scheduler")})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith(MockitoExtension.class)
public class EventSchedulerTests {

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

    private Scheduler<TestWatchTarget, TestWatchParty, Integer> scheduler;
    private TestData                                            data;

    @Mock
    private Planifiable<TestWatchTarget> planifiable;

    @BeforeEach
    public void setup() {

        this.data      = new TestData();
        this.scheduler = new EventScheduler<>(this.data.dataBank(), TestWatchParty::getId);
    }

    @Nested
    @DisplayName("Single Scheduling")
    class SingleScheduling {

        @Test
        @DisplayName("When no overlaps, should not conflict")
        void testWhenNoOverlapsShouldNotConflict() {

            Instant                           scheduleAt = TestData.BASE_DATETIME.plus(1, ChronoUnit.DAYS);
            ScheduleSpotData<TestWatchTarget> spot       = new TestSpot(data.target2, scheduleAt, 1);

            assertTrue(scheduler.canSchedule(spot), "The event can't be scheduled.");

            var plan = assertDoesNotThrow(
                    () -> scheduler.schedule(spot),
                    "An error occurred while scheduling the event."
            );

            var data = assertSingleCreateAction(plan).what();

            assertEquals(1, data.firstEpisode(), "First episode mismatch.");
            assertEquals(1, data.episodeCount(), "Episode count mismatch.");
            assertEquals(scheduleAt, data.startingAt(), "Starting datetime mismatch.");
        }

        @Test
        @DisplayName("When overlaps, should conflict")
        void testWhenOverlapsShouldConfit() {

            ScheduleSpotData<TestWatchTarget> spot = new TestSpot(data.target2, TestData.BASE_DATETIME, 1);
            assertFalse(scheduler.canSchedule(spot), "The event can be scheduled.");
            assertThrows(NotSchedulableException.class, () -> scheduler.schedule(spot));
        }

        @Test
        @DisplayName("Next event should have correct first episode")
        void testThatNextEventShouldHaveCorrectFirstEpisode() {

            Instant                           scheduleAt = TestData.BASE_DATETIME.plus(1, ChronoUnit.DAYS);
            ScheduleSpotData<TestWatchTarget> spot       = new TestSpot(data.target1, scheduleAt, 1);

            assertTrue(scheduler.canSchedule(spot), "The event can't be scheduled.");

            var plan = assertDoesNotThrow(() -> scheduler.schedule(spot));
            var data = assertSingleCreateAction(plan).what();

            assertEquals(
                    7,
                    data.firstEpisode(),
                    "First episode should be calculated after all existing events for the target."
            );
            assertEquals(1, data.episodeCount(), "Episode count mismatch.");
        }

        @Test
        @DisplayName("When invalid episode count, should fail")
        void testWhenInvalidEpisodeCountShouldFail() {

            ScheduleSpotData<TestWatchTarget> spot = new TestSpot(data.target2, TestData.BASE_DATETIME, -1);
            assertThrows(InvalidSchedulingDurationException.class, () -> scheduler.canSchedule(spot));
        }

    }

    @Nested
    @DisplayName("Event Merging")
    class EventMerging {

        @Test
        @DisplayName("When scheduled after, should merge with previous")
        public void testWhenScheduledAfterShouldMergeWithPrevious() {

            ScheduleSpotData<TestWatchTarget> spot = new TestSpot(
                    data.target1,
                    data.partyB2.getEndingAt().plus(5, ChronoUnit.MINUTES),
                    1
            );

            assertTrue(scheduler.canSchedule(spot), "The event can't be scheduled.");

            var plan   = assertDoesNotThrow(() -> scheduler.schedule(spot));
            var update = assertSingleUpdateAction(plan);

            assertEquals(data.partyB2.getId(), update.targetId(), "The plan should target the left party for update.");

            TestWatchParty mockParty = new TestWatchParty(
                    data.target1,
                    data.partyB2.getStartingAt(),
                    data.partyB2.getEpisodeCount(),
                    data.partyB2.getFirstEpisode()
            );

            update.hook().accept(mockParty);

            assertEquals(3, mockParty.getEpisodeCount(), "Episode count should be merged.");
            assertEquals(
                    data.partyB2.getStartingAt(),
                    mockParty.getStartingAt(),
                    "Starting time should not change on a left merge."
            );
        }

        @Test
        @DisplayName("When scheduled before, should merge with next")
        public void testWhenScheduledBeforeShouldMergeWithPrevious() {

            ScheduleSpotData<TestWatchTarget> spot = new TestSpot(
                    data.target1,
                    data.partyB1.getStartingAt().minus(Duration.ofMinutes(24)).minus(5, ChronoUnit.MINUTES),
                    1
            );
            Instant scheduleAt = spot.getStartingAt();

            assertTrue(scheduler.canSchedule(spot), "The event can't be scheduled.");

            var plan   = assertDoesNotThrow(() -> scheduler.schedule(spot));
            var update = assertSingleUpdateAction(plan);

            assertEquals(
                    data.partyB1.getId(),
                    update.targetId(),
                    "The plan should target the right party for update."
            );

            TestWatchParty mockParty = new TestWatchParty(
                    data.target1,
                    data.partyB1.getStartingAt(),
                    data.partyB1.getEpisodeCount(),
                    data.partyB1.getFirstEpisode()
            );

            update.hook().accept(mockParty);

            assertEquals(3, mockParty.getEpisodeCount(), "Episode count should be merged.");
            assertEquals(3, mockParty.getFirstEpisode(), "First episode should be correct based on previous events.");
            assertEquals(
                    scheduleAt,
                    mockParty.getStartingAt(),
                    "Starting time should be updated to the new spot's time."
            );
        }

        @Test
        @DisplayName("When scheduled between, should merge with previous and next (Sandwich Merging)")
        public void testWhenScheduledBetweenShouldMergeWithPreviousAndNext() {

            ScheduleSpotData<TestWatchTarget> spot = new TestSpot(
                    data.target1,
                    data.partyB1.getEndingAt().plus(5, ChronoUnit.MINUTES),
                    1
            );
            Instant scheduleAt = spot.getStartingAt();

            data.partyB2.setStartingAt(scheduleAt.plus(spot.getDuration()).plus(5, ChronoUnit.MINUTES));

            assertTrue(scheduler.canSchedule(spot), "The event can't be scheduled.");
            var plan = assertDoesNotThrow(() -> scheduler.schedule(spot));

            assertPlanActions(plan, 0, 1, 1);

            var update = plan.updates().getFirst();
            var delete = plan.deletes().getFirst();

            assertEquals(data.partyB1.getId(), update.targetId(), "The update should target the first party.");
            assertEquals(data.partyB2.getId(), delete.targetId(), "The delete should target the second party.");

            TestWatchParty mockParty = new TestWatchParty(
                    data.target1,
                    data.partyB1.getStartingAt(),
                    data.partyB1.getEpisodeCount(),
                    data.partyB1.getFirstEpisode()
            );
            update.hook().accept(mockParty);

            assertEquals(
                    5,
                    mockParty.getEpisodeCount(),
                    "Episode count should be the sum of all three spots (2 + 1 + 2)."
            );
            assertEquals(
                    data.partyB1.getStartingAt(),
                    mockParty.getStartingAt(),
                    "Starting time should be from the first party."
            );
        }

        @Test
        @DisplayName("When scheduled at magnet range limit, should merge")
        void testWhenScheduledAtMagnetRangeLimitShouldMerge() {

            Instant scheduleAt = data.partyA1.getEndingAt().plus(EventScheduler.MERGE_MAGNET_LIMIT);

            ScheduleSpotData<TestWatchTarget> spot = new TestSpot(data.target1, scheduleAt, 1);

            assertTrue(scheduler.canSchedule(spot), "Should be able to schedule at the exact merge boundary.");
            var plan = scheduler.schedule(spot);
            assertSingleUpdateAction(plan);
        }

        @Test
        @DisplayName("When scheduled outside magnet range, should not merge")
        void testWhenScheduledOutsideMagnetRangeShouldNotMerge() {

            Instant scheduleAt = data.partyA1.getEndingAt().plus(EventScheduler.MERGE_MAGNET_LIMIT).plusSeconds(1);

            ScheduleSpotData<TestWatchTarget> spot = new TestSpot(data.target1, scheduleAt, 1);

            assertTrue(scheduler.canSchedule(spot), "Should be able to schedule just outside the merge boundary.");
            var plan = scheduler.schedule(spot);
            assertSingleCreateAction(plan);
        }

    }

    @Nested
    @DisplayName("Event delaying")
    class EventDelaying {

        @Test
        @DisplayName("When no overlaps, should not conflict")
        public void testWhenNoOverlapsShouldNotConflict() {

            Duration delay = Duration.ofHours(1);

            var plan = assertDoesNotThrow(() -> scheduler.delay(
                    TestData.BASE_DATETIME,
                    Duration.ofMinutes(60),
                    delay
            ));

            var update = assertSingleUpdateAction(plan);

            assertEquals(data.partyA1.getId(), update.targetId(), "Wrong item targeted for delay.");

            TestWatchParty mockParty = new TestWatchParty(
                    data.target1,
                    data.partyA1.getStartingAt(),
                    0,
                    0
            );
            update.hook().accept(mockParty);

            assertEquals(
                    data.partyA1.getStartingAt().plus(delay),
                    mockParty.getStartingAt(),
                    "Delay duration not respected."
            );
        }

        @Test
        @DisplayName("When overlaps, should conflict")
        public void testWhenOverlapsShouldConflict() {

            assertThrows(
                    DelayOverlapException.class, () -> scheduler.delay(
                            TestData.BASE_DATETIME,
                            Duration.ofMinutes(60),
                            Duration.between(data.partyA1.getStartingAt(), data.partyB1.getStartingAt())
                    )
            );
        }

    }

    @Nested
    @DisplayName("Calibration")
    class ScheduleCalibration {

        @Test
        @DisplayName("When no calibration is needed, should not update anything")
        public void testWhenNoCalibrationIsNeededShouldNotUpdateAnything() {

            var plan = assertDoesNotThrow(() -> scheduler.calibrate());
            assertEmptyPlan(plan);
        }

        @Test
        @DisplayName("When watch progress is changed, should update existing")
        public void testWhenWatchProgressIsChangedShouldUpdateExisting() {

            data.target1.setWatched(1);
            var plan = assertDoesNotThrow(() -> scheduler.calibrate());

            assertPlanActions(plan, 0, 3, 0);
            var update = assertSingleUpdateAction(plan, action -> action.targetId().equals(data.partyA1.getId()));

            var mockParty = new TestWatchParty(null, null, 0, data.partyA1.getFirstEpisode());
            update.hook().accept(mockParty);

            assertEquals(2, mockParty.getFirstEpisode(), "First episode for partyA1 should be recalibrated to 2.");
        }

        @Test
        @DisplayName("When watch progress is completed, should delete existing")
        public void testWhenWatchProgressIsCompletedShouldDeleteExisting() {

            data.target1.setWatched(12);
            var plan = assertDoesNotThrow(() -> scheduler.calibrate());
            assertPlanActions(plan, 0, 0, 3);
        }

        @Test
        @DisplayName("When watch progress is close to completion, should update and delete existing")
        public void testWhenWatchProgressIsCloseToCompletionShouldUpdateAndDeleteExisting() {

            data.target1.setWatched(8);
            var plan = assertDoesNotThrow(() -> scheduler.calibrate());

            assertPlanActions(plan, 0, 2, 1);

            var delete = plan.deletes().getFirst();

            assertEquals(data.partyB2.getId(), delete.targetId(), "partyB2 should be deleted.");
        }

        @Test
        @DisplayName("When watch progress and total are changed, should update, delete and shrink existing")
        public void testWhenWatchProgressAndTotalAreChangedShouldUpdateDeleteAndShrinkExisting() {

            data.target1.setWatched(2);
            data.target1.setTotal(3);

            var plan = assertDoesNotThrow(() -> scheduler.calibrate());
            assertPlanActions(plan, 0, 1, 2);

            var update = plan.updates().getFirst();

            TestWatchParty mockParty = new TestWatchParty(
                    data.target1,
                    data.partyA1.getStartingAt(),
                    data.partyA1.getEpisodeCount(),
                    data.partyA1.getFirstEpisode()
            );
            update.hook().accept(mockParty);

            assertEquals(3, mockParty.getFirstEpisode(), "First episode should be updated to 3.");
            assertEquals(1, mockParty.getEpisodeCount(), "Episode count should be shrunk to 1 to fit total.");
        }

        @Test
        @DisplayName("When negative total, should update normally")
        void testWhenNegativeTotalShouldUpdateNormally() {

            data.target1.setTotal(-12);
            data.target1.setWatched(2);

            var plan = assertDoesNotThrow(() -> scheduler.calibrate());
            assertPlanActions(plan, 0, 3, 0);

            var update = assertSingleUpdateAction(
                    plan,
                    action -> action.targetId().equals(data.partyB2.getId())
            );
            var mockParty = new TestWatchParty(null, null, 0, 0);

            update.hook().accept(mockParty);

            assertEquals(7, mockParty.getFirstEpisode(), "partyB2 should start at episode 7 (5+2).");
        }

    }

    @Nested
    @DisplayName("Querying")
    class Querying {

        @Test
        @DisplayName("Should find next")
        void shouldFindNext() {

            Optional<TestWatchParty> queryResult = scheduler.findNext(data.partyA1.getStartingAt());
            assertTrue(queryResult.isPresent(), "Should find the next event.");
            assertEquals(data.partyB1.getId(), queryResult.get().getId(), "Should return partyB1 as the next event.");
        }

        @Test
        @DisplayName("Should not find next")
        void shouldNotFindNext() {

            Optional<TestWatchParty> queryResult = scheduler.findNext(data.partyB2.getStartingAt());
            assertTrue(queryResult.isEmpty(), "Should not find any event after the last one.");
        }

        @Test
        @DisplayName("Should find previous")
        void shouldFindPrevious() {

            Optional<TestWatchParty> queryResult = scheduler.findPrevious(data.partyB2.getStartingAt());

            assertTrue(queryResult.isPresent(), "Should find the previous event.");
            assertEquals(
                    data.partyB1.getId(),
                    queryResult.get().getId(),
                    "Should return partyB1 as the previous event."
            );
        }

        @Test
        @DisplayName("Should not find previous")
        void shouldNotFindPrevious() {

            Optional<TestWatchParty> queryResult = scheduler.findPrevious(data.partyA1.getStartingAt());
            assertTrue(queryResult.isEmpty(), "Should not find any event before the first one.");
        }

        @Test
        @DisplayName("Should find next with watch target")
        void shouldFindNextWithWatchTarget() {
            // Add an event for another target
            TestWatchParty partyC = new TestWatchParty(
                    data.target2,
                    TestData.BASE_DATETIME.plus(1, ChronoUnit.HOURS),
                    1,
                    1
            );
            scheduler = new EventScheduler<>(List.of(data.partyA1, partyC, data.partyB1), TestWatchParty::getId);

            var nextForTarget = scheduler.findNext(data.partyA1.getStartingAt(), data.target1);
            assertTrue(nextForTarget.isPresent(), "Should find next for target1.");
            assertEquals(data.partyB1.getId(), nextForTarget.get().getId(), "Next for target1 should be partyB1.");

        }

        @Test
        @DisplayName("Should not find next with watch target")
        void shouldNotFindNextWithWatchTarget() {
            // Add an event for another target
            TestWatchParty partyC = new TestWatchParty(
                    data.target2,
                    TestData.BASE_DATETIME.plus(1, ChronoUnit.HOURS),
                    1,
                    1
            );
            scheduler = new EventScheduler<>(List.of(data.partyA1, partyC, data.partyB1), TestWatchParty::getId);

            var nextForTarget = scheduler.findNext(data.partyA1.getStartingAt(), data.target2);
            assertTrue(nextForTarget.isPresent(), "Should find next for target2.");
            assertEquals(partyC.getId(), nextForTarget.get().getId(), "Next for target2 should be partyC.");
        }

    }

}