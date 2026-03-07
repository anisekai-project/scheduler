package fr.anisekai.scheduler.event;

import fr.anisekai.scheduler.event.interfaces.entities.Planifiable;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class TestWatchParty implements Planifiable<TestWatchTarget> {

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
