package zingzingzingbah.engine;

import zingzingzingbah.model.Character;
import zingzingzingbah.model.Kart;
import zingzingzingbah.model.RaceEvent;
import zingzingzingbah.track.Track;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Deque;

/**
 * The core racing engine that coordinates all karts, checkpoints,
 * lap counting, and race position sorting.
 *
 * Java 2 concepts demonstrated:
 *   - ArrayList<Kart>              — list of all racers
 *   - LinkedList as Queue<RaceEvent<?>>  — FIFO queue for race events
 *   - ArrayDeque as Stack (Deque<String>) — LIFO log of race messages
 *   - Generics via RaceEvent<T>    — typed event payloads
 *   - Comparator lambda            — sorting karts by race progress
 */
public class RacingEngine {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------
    public static final int TOTAL_LAPS = 3;

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------
    private final Track track;

    /** All karts in the race (player at index 0). */
    private final List<Kart> karts = new ArrayList<>();

    /**
     * FIFO event queue — stores race events as they happen.
     * Java 2 concept: Queue (LinkedList implements Queue<E>)
     */
    private final Queue<RaceEvent<?>> eventQueue = new LinkedList<>();

    /**
     * Race log stack — push events; top = most recent.
     * Java 2 concept: Stack / Deque (ArrayDeque used as a stack)
     */
    private final Deque<String> raceLog = new ArrayDeque<>();

    private static final int LOG_MAX = 6;   // keep last 6 messages

    private boolean raceFinished = false;
    private int finishPosition   = 1;   // position player finished in

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------
    public RacingEngine(Track track, Character playerCharacter,
                        List<Character> allCharacters) {
        this.track = track;

        double[] startX    = track.getStartX();
        double[] startY    = track.getStartY();
        double startAngle  = track.getStartAngle();

        // AI speed multipliers — slight variation for interesting races
        double[] aiMults = { 0.90, 0.95, 0.88, 0.93 };

        // Kart 0 = player
        karts.add(new Kart(playerCharacter, startX[0], startY[0], startAngle, true, 1.0));

        // Karts 1-4 = AI  (pick the other 4 characters)
        int aiIndex = 0;
        for (Character c : allCharacters) {
            if (aiIndex >= 4) break;
            if (c.getName().equals(playerCharacter.getName())) continue;
            karts.add(new Kart(c, startX[aiIndex + 1], startY[aiIndex + 1],
                               startAngle, false, aiMults[aiIndex]));
            aiIndex++;
        }

        logEvent("Race starting! Good luck, " + playerCharacter.getName() + "!");
    }

    // -----------------------------------------------------------------------
    // Game loop update
    // -----------------------------------------------------------------------
    /**
     * Advances simulation by one time step.
     * @param delta seconds since last frame
     */
    public void update(double delta) {
        if (raceFinished) return;

        double[] wpX = track.getWaypointX();
        double[] wpY = track.getWaypointY();

        for (Kart kart : karts) {
            // Update physics
            kart.update(delta, wpX, wpY);

            // Track boundary check
            boolean on = track.isOnTrack(kart.getX(), kart.getY());
            kart.setOnTrack(on);

            // Checkpoint / lap logic
            checkCheckpoints(kart);
        }

        // Sort karts by progress to assign race positions
        updatePositions();

        // Process event queue (drain up to 3 per frame to avoid spam)
        int processed = 0;
        while (!eventQueue.isEmpty() && processed < 3) {
            RaceEvent<?> ev = eventQueue.poll();
            handleEvent(ev);
            processed++;
        }
    }

    // -----------------------------------------------------------------------
    // Checkpoint & lap detection
    // -----------------------------------------------------------------------
    private void checkCheckpoints(Kart kart) {
        if (kart.isFinished()) return;

        int next = kart.getNextCheckpoint();

        if (track.isAtCheckpoint(kart.getX(), kart.getY(), next)) {
            // Advance to next checkpoint
            int newNext = (next + 1) % Track.NUM_CHECKPOINTS;
            kart.setNextCheckpoint(newNext);

            // Checkpoint 0 is the start/finish line
            if (next == 0) {
                int newLap = kart.getLap() + 1;
                kart.setLap(newLap);

                if (kart.isPlayer()) {
                    if (newLap <= TOTAL_LAPS) {
                        enqueueEvent(new RaceEvent<>(
                                RaceEvent.EventType.LAP_COMPLETE,
                                "Lap " + newLap + " / " + TOTAL_LAPS + " complete!"));
                    }
                }

                if (newLap >= TOTAL_LAPS) {
                    kart.setFinished(true);
                    if (kart.isPlayer()) {
                        raceFinished  = true;
                        finishPosition = kart.getRacePosition();
                        enqueueEvent(new RaceEvent<>(
                                RaceEvent.EventType.RACE_FINISH,
                                "FINISHED in position " + finishPosition + "!"));
                    }
                }
            } else {
                if (kart.isPlayer()) {
                    enqueueEvent(new RaceEvent<>(
                            RaceEvent.EventType.CHECKPOINT_PASS,
                            "Checkpoint " + next));
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Position calculation
    // -----------------------------------------------------------------------
    /**
     * Sorts karts by race progress (laps * checkpoints + checkpoints passed)
     * then assigns racePosition 1..N.
     */
    private void updatePositions() {
        int numCp = Track.NUM_CHECKPOINTS;

        karts.sort(Comparator.comparingDouble((Kart k) -> {
            // "checkpoints passed" = (nextCheckpoint - 1 + NUM_CPs) % NUM_CPs
            int cpPassed = (k.getNextCheckpoint() + numCp - 1) % numCp;
            return -(k.getLap() * numCp + cpPassed);  // negative = sort descending
        }));

        for (int i = 0; i < karts.size(); i++) {
            karts.get(i).setRacePosition(i + 1);
        }
    }

    // -----------------------------------------------------------------------
    // Event handling (Queue consumer)
    // -----------------------------------------------------------------------
    private void handleEvent(RaceEvent<?> event) {
        logEvent(event.getData().toString());
    }

    // -----------------------------------------------------------------------
    // Event queue helpers
    // -----------------------------------------------------------------------
    /**
     * Adds a typed event to the FIFO queue.
     * Java 2 concept: Queue.add()
     */
    public <T> void enqueueEvent(RaceEvent<T> event) {
        eventQueue.add(event);
    }

    /**
     * Pushes a message onto the log stack (top = most recent).
     * Java 2 concept: Deque.push() acts as stack push
     */
    private void logEvent(String message) {
        raceLog.push(message);
        while (raceLog.size() > LOG_MAX) {
            raceLog.pollLast();   // trim oldest from bottom
        }
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------
    public List<Kart> getKarts()          { return karts; }

    /** Returns the player kart (always index 0). */
    public Kart getPlayerKart()           { return karts.get(0); }

    public boolean isRaceFinished()       { return raceFinished; }
    public int getFinishPosition()        { return finishPosition; }

    /**
     * Returns recent log messages as an array (index 0 = most recent).
     * Java 2 concept: Deque iteration (top-of-stack first)
     */
    public String[] getRecentLog() {
        return raceLog.stream().limit(4).toArray(String[]::new);
    }
}
