package com.zingzingracing.racing;

import com.badlogic.gdx.math.Vector2;
import com.zingzingracing.characters.BoohbahCharacter;
import com.zingzingracing.track.Track;
import com.zingzingracing.track.TrackWaypoint;
import java.util.ArrayList;

/**
 * AI-controlled racer that follows track waypoints.
 * Uses a Queue-like approach for waypoint ordering (Java 2 concept).
 */
public class AIRacer extends Racer {

    private final float difficulty; // 0.5 = easy, 1.0 = hard
    private float rubberBandBoost = 1.0f;
    private boolean countdownDone = false;

    // Uses a stack for "memory" of recent actions (Java 2 concept)
    private final java.util.ArrayDeque<Float> recentAngles = new java.util.ArrayDeque<>();

    public AIRacer(BoohbahCharacter character, float startX, float startY, float difficulty) {
        super(character, startX, startY, 90f);
        this.difficulty = difficulty;
        // AI cars slightly slower than max to give player a chance
        this.maxSpeed = character.getTopSpeed() * (0.75f + difficulty * 0.15f);
    }

    public void setCountdownDone(boolean done) {
        this.countdownDone = done;
    }

    public void setRubberBandBoost(float boost) {
        this.rubberBandBoost = boost;
    }

    @Override
    public void update(float delta, Track track) {
        if (finishedRace) return;
        raceTime += delta;
        if (!countdownDone) return;

        ArrayList<TrackWaypoint> waypoints = track.getWaypoints();
        if (!waypoints.isEmpty()) {
            totalWaypoints = waypoints.size();
            TrackWaypoint target = waypoints.get(nextWaypoint % totalWaypoints);
            navigateTo(target.position.x, target.position.y, delta);
            if (target.contains(x, y)) {
                nextWaypoint++;
            }
        }

        applyMovement(delta, track);
        checkStartLineAI();

        // Track recent angles (stack usage for smoothing)
        recentAngles.push(angle);
        if (recentAngles.size() > 5) recentAngles.pollLast();
    }

    private void navigateTo(float tx, float ty, float delta) {
        float dx = tx - x;
        float dy = ty - y;
        float targetAngle = (float) Math.toDegrees(Math.atan2(dy, dx));

        float angleDiff = targetAngle - angle;
        // Normalize to [-180, 180]
        while (angleDiff > 180) angleDiff -= 360;
        while (angleDiff < -180) angleDiff += 360;

        // Turn toward target
        float turnAmount = handling * delta * difficulty;
        if (Math.abs(angleDiff) > 5f) {
            if (angleDiff > 0) angle += Math.min(turnAmount, angleDiff);
            else               angle -= Math.min(turnAmount, -angleDiff);
        }

        // Accelerate / brake based on how sharp the turn is
        float sharpness = Math.abs(angleDiff);
        float dst = (float) Math.sqrt(dx * dx + dy * dy);

        if (sharpness < 40f || dst < 80f) {
            accelerate(delta);
            speed *= rubberBandBoost;
            speed = Math.min(speed, maxSpeed);
        } else {
            // Slow for sharp corners
            brake(delta);
        }
    }

    private boolean crossedStart = false;
    private void checkStartLineAI() {
        float startX = Track.TRACK_CENTER_X;
        float startYMin = Track.TRACK_CENTER_Y + Track.INNER_RY;
        float startYMax = Track.TRACK_CENTER_Y + Track.OUTER_RY;
        float zone = 22f;

        boolean nearLine = Math.abs(x - startX) < zone && y > startYMin && y < startYMax;
        if (nearLine && !crossedStart && nextWaypoint >= totalWaypoints - 2 && currentLap > 0) {
            currentLap++;
            nextWaypoint = 0;
            crossedStart = true;
        } else if (nearLine && !crossedStart && currentLap == 0) {
            currentLap = 1;
            crossedStart = true;
        } else if (!nearLine) {
            crossedStart = false;
        }
    }
}
