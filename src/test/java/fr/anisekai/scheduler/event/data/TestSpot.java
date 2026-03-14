package fr.anisekai.scheduler.event.data;

import fr.anisekai.scheduler.event.interfaces.ScheduleSpotData;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class TestSpot implements ScheduleSpotData<TestWatchTarget> {

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
