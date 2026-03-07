package fr.anisekai.scheduler.event;

import fr.anisekai.scheduler.event.interfaces.entities.WatchTarget;

public class TestWatchTarget implements WatchTarget {

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
