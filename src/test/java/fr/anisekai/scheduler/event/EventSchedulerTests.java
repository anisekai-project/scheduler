package fr.anisekai.scheduler.event;

import fr.anisekai.scheduler.event.data.TestSpot;
import fr.anisekai.scheduler.event.data.TestWatchParty;
import fr.anisekai.scheduler.event.data.TestWatchTarget;
import fr.anisekai.scheduler.event.exceptions.DelayOverlapException;
import fr.anisekai.scheduler.event.exceptions.InvalidSchedulingDurationException;
import fr.anisekai.scheduler.event.exceptions.NotSchedulableException;
import fr.anisekai.scheduler.event.interfaces.ScheduleSpotData;
import fr.anisekai.scheduler.event.interfaces.Scheduler;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.anisekai.scheduler.ActionPlanAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventScheduler")
@Tags({@Tag("unit-test"), @Tag("event-scheduler")})
@TestMethodOrder(MethodOrderer.DisplayName.class)
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

    @BeforeEach
    public void setup() {

        this.data      = new TestData();
        this.scheduler = new EventScheduler<>(this.data.dataBank(), TestWatchParty::getId);
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling - No Conflicts")
    public void testSingleSchedulingNoConflict() {

        Instant                           scheduleAt = TestData.BASE_DATETIME.plus(1, ChronoUnit.DAYS);
        ScheduleSpotData<TestWatchTarget> spot       = new TestSpot(this.data.target2, scheduleAt, 1);

        assertTrue(this.scheduler.canSchedule(spot), "The event can't be scheduled.");

        var plan = assertDoesNotThrow(
                () -> this.scheduler.schedule(spot),
                "An error occurred while scheduling the event."
        );

        var data = assertSingleCreateAction(plan).what();

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

        var plan = assertDoesNotThrow(() -> this.scheduler.schedule(spot));
        var data = assertSingleCreateAction(plan).what();

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

        var plan   = assertDoesNotThrow(() -> this.scheduler.schedule(spot));
        var update = assertSingleUpdateAction(plan);

        assertEquals(this.data.partyB2.getId(), update.targetId(), "The plan should target the left party for update.");

        TestWatchParty mockParty = new TestWatchParty(
                this.data.target1,
                this.data.partyB2.getStartingAt(), this.data.partyB2.getEpisodeCount(),
                this.data.partyB2.getFirstEpisode()
        );

        update.hook().accept(mockParty);

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

        var plan   = assertDoesNotThrow(() -> this.scheduler.schedule(spot));
        var update = assertSingleUpdateAction(plan);

        assertEquals(
                this.data.partyB1.getId(),
                update.targetId(),
                "The plan should target the right party for update."
        );

        TestWatchParty mockParty = new TestWatchParty(
                this.data.target1,
                this.data.partyB1.getStartingAt(), this.data.partyB1.getEpisodeCount(),
                this.data.partyB1.getFirstEpisode()
        );

        update.hook().accept(mockParty);

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
        var plan = assertDoesNotThrow(() -> this.scheduler.schedule(spot));

        assertPlanActions(plan, 0, 1, 1);

        var update = plan.updates().getFirst();
        var delete = plan.deletes().getFirst();

        assertEquals(this.data.partyB1.getId(), update.targetId(), "The update should target the first party.");
        assertEquals(this.data.partyB2.getId(), delete.targetId(), "The delete should target the second party.");

        TestWatchParty mockParty = new TestWatchParty(
                this.data.target1,
                this.data.partyB1.getStartingAt(), this.data.partyB1.getEpisodeCount(),
                this.data.partyB1.getFirstEpisode()
        );
        update.hook().accept(mockParty);

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

        var plan = assertDoesNotThrow(() -> this.scheduler.delay(
                TestData.BASE_DATETIME,
                Duration.ofMinutes(60),
                delay
        ));

        var update = assertSingleUpdateAction(plan);

        assertEquals(this.data.partyA1.getId(), update.targetId(), "Wrong item targeted for delay.");

        TestWatchParty mockParty = new TestWatchParty(
                this.data.target1,
                this.data.partyA1.getStartingAt(),
                0,
                0
        );
        update.hook().accept(mockParty);

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

        var plan = assertDoesNotThrow(() -> this.scheduler.calibrate());
        assertEmptyPlan(plan);
    }

    @Test
    @DisplayName("Scheduler | Calibration - Upstream @ Update Only")
    public void testCalibrationUpstreamUpdateOnly() {

        this.data.target1.setWatched(1);
        var plan = assertDoesNotThrow(() -> this.scheduler.calibrate());

        assertPlanActions(plan, 0, 3, 0);
        var update = assertSingleUpdateAction(plan, action -> action.targetId().equals(this.data.partyA1.getId()));

        var mockParty = new TestWatchParty(null, null, 0, this.data.partyA1.getFirstEpisode());
        update.hook().accept(mockParty);

        assertEquals(2, mockParty.getFirstEpisode(), "First episode for partyA1 should be recalibrated to 2.");
    }

    @Test
    @DisplayName("Scheduler | Calibration - Upstream @ Delete Only")
    public void testCalibrationUpstreamDeleteOnly() {

        this.data.target1.setWatched(12);
        var plan = assertDoesNotThrow(() -> this.scheduler.calibrate());
        assertPlanActions(plan, 0, 0, 3);
    }

    @Test
    @DisplayName("Scheduler | Calibration - Upstream @ Update + Delete")
    public void testCalibrationUpstreamUpdateDelete() {

        this.data.target1.setWatched(8);
        var plan = assertDoesNotThrow(() -> this.scheduler.calibrate());

        assertPlanActions(plan, 0, 2, 1);

        var delete = plan.deletes().getFirst();

        assertEquals(this.data.partyB2.getId(), delete.targetId(), "partyB2 should be deleted.");
    }

    @Test
    @DisplayName("Scheduler | Calibration - Upstream @ Update + Delete + Shrink")
    public void testCalibrationUpstreamUpdateDeleteShrink() {

        this.data.target1.setWatched(2);
        this.data.target1.setTotal(3);

        var plan = assertDoesNotThrow(() -> this.scheduler.calibrate());
        assertPlanActions(plan, 0, 1, 2);

        var update = plan.updates().getFirst();

        TestWatchParty mockParty = new TestWatchParty(
                this.data.target1,
                this.data.partyA1.getStartingAt(), this.data.partyA1.getEpisodeCount(),
                this.data.partyA1.getFirstEpisode()
        );
        update.hook().accept(mockParty);

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
        var plan = this.scheduler.schedule(spot);
        assertSingleUpdateAction(plan);
    }

    @Test
    @DisplayName("Scheduler | Additional | No Merge Outside Magnet Limit")
    void testNoMergeOutsideBoundary() {

        Instant scheduleAt = this.data.partyA1.getEndingAt()
                                              .plus(EventScheduler.MERGE_MAGNET_LIMIT)
                                              .plusSeconds(1);
        ScheduleSpotData<TestWatchTarget> spot = new TestSpot(this.data.target1, scheduleAt, 1);

        assertTrue(this.scheduler.canSchedule(spot), "Should be able to schedule just outside the merge boundary.");
        var plan = this.scheduler.schedule(spot);
        assertSingleCreateAction(plan);
    }

    @Test
    @DisplayName("Scheduler | Additional | Calibration with Negative (Estimated) Total")
    void testCalibrationWithEstimatedTotal() {

        this.data.target1.setTotal(-12);
        this.data.target1.setWatched(2);

        var plan = assertDoesNotThrow(() -> this.scheduler.calibrate());
        assertPlanActions(plan, 0, 3, 0);

        var update    = assertSingleUpdateAction(plan, action -> action.targetId().equals(this.data.partyB2.getId()));
        var mockParty = new TestWatchParty(null, null, 0, 0);

        update.hook().accept(mockParty);

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

        var plan   = assertDoesNotThrow(() -> this.scheduler.schedule(spot));
        var update = assertSingleUpdateAction(plan, action -> action.targetId().equals(this.data.partyB1.getId()));

        TestWatchParty mockParty = new TestWatchParty(
                this.data.target1,
                this.data.partyB1.getStartingAt(),
                this.data.partyB1.getEpisodeCount(),
                this.data.partyB1.getFirstEpisode()
        );
        update.hook().accept(mockParty);

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
