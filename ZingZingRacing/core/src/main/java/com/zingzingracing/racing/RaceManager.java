package com.zingzingracing.racing;

import com.zingzingracing.ZingZingRacing;
import com.zingzingracing.track.Track;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Manages all racers, positions, and race state.
 * Uses ArrayList, Queue, and generics — Java 2 concepts.
 */
public class RaceManager {

    private final ArrayList<Racer> racers;           // all racers
    private final Queue<Racer> finishQueue;          // queue of finishers in order
    private final PlayerRacer player;
    private final ArrayList<AIRacer> aiRacers;

    private float countdown = 3.0f;
    private boolean raceStarted = false;
    private boolean raceOver = false;
    private final int totalLaps;
    private final Track track;

    public RaceManager(PlayerRacer player, ArrayList<AIRacer> aiRacers, Track track, int totalLaps) {
        this.player = player;
        this.aiRacers = aiRacers;
        this.track = track;
        this.totalLaps = totalLaps;
        this.racers = new ArrayList<>();
        this.finishQueue = new LinkedList<>();

        racers.add(player);
        racers.addAll(aiRacers);
    }

    public void update(float delta) {
        if (!raceStarted) {
            countdown -= delta;
            if (countdown <= 0) {
                raceStarted = true;
                countdown = 0;
                player.setCountdownDone(true);
                for (AIRacer ai : aiRacers) ai.setCountdownDone(true);
            }
            return;
        }

        // Update all racers
        for (Racer r : racers) {
            r.update(delta, track);
        }

        // Check finishes
        for (Racer r : racers) {
            if (!r.isFinished() && r.getCurrentLap() > totalLaps) {
                r.setFinished(true);
                finishQueue.offer(r);
            }
        }

        // Rubber band AI: boost lagging AI, slow leading AI
        applyRubberBanding();

        // Update positions
        updatePositions();

        // Race is over when player finishes or all racers finish
        if (player.isFinished()) {
            raceOver = true;
        }
    }

    private void applyRubberBanding() {
        float playerProgress = player.getRaceProgress();
        for (AIRacer ai : aiRacers) {
            float diff = playerProgress - ai.getRaceProgress();
            float boost = 1.0f;
            if (diff > 200f) boost = 1.08f;       // player far ahead — speed up AI
            else if (diff < -200f) boost = 0.90f; // AI far ahead — slow down
            ai.setRubberBandBoost(boost);
        }
    }

    private void updatePositions() {
        // Sort by race progress descending
        racers.sort((r1, r2) -> Float.compare(r2.getRaceProgress(), r1.getRaceProgress()));
        for (int i = 0; i < racers.size(); i++) {
            racers.get(i).setPosition(i + 1);
        }
    }

    public float getCountdown() { return countdown; }
    public boolean isRaceStarted() { return raceStarted; }
    public boolean isRaceOver() { return raceOver; }
    public ArrayList<Racer> getRacers() { return racers; }
    public Queue<Racer> getFinishQueue() { return finishQueue; }
    public PlayerRacer getPlayer() { return player; }
    public int getTotalLaps() { return totalLaps; }
}
