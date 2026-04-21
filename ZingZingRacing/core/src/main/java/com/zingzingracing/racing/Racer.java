package com.zingzingracing.racing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.zingzingracing.characters.BoohbahCharacter;
import com.zingzingracing.track.Track;
import com.zingzingracing.track.TrackWaypoint;
import java.util.ArrayList;

/**
 * Base Racer class — Mario Kart PC style physics:
 * - Top-down with angular turning
 * - Speed-based turning radius
 * - Off-road slowdown
 * - Lap tracking via waypoints
 */
public abstract class Racer {
    // Position & physics
    protected float x, y;
    protected float angle;       // degrees, 90 = up
    protected float speed;
    protected float maxSpeed;
    protected float acceleration;
    protected float handling;    // turning speed
    protected float friction = 0.94f;
    protected float offRoadFriction = 0.80f;

    // Race state
    protected int currentLap;
    protected int nextWaypoint;
    protected boolean finishedRace;
    protected float raceTime;
    protected int position; // race position (1st, 2nd, etc.)
    protected int totalWaypoints;

    // Lap detection
    private boolean crossedStartLine;
    private static final float START_X = Track.TRACK_CENTER_X;
    private static final float START_Y_MIN = Track.TRACK_CENTER_Y + Track.INNER_RY;
    private static final float START_Y_MAX = Track.TRACK_CENTER_Y + Track.OUTER_RY;
    private static final float START_ZONE = 20f;

    // Character data
    protected BoohbahCharacter character;
    protected String name;

    // Visual
    protected float bodyRadius = 14f;

    public Racer(BoohbahCharacter character, float startX, float startY, float startAngle) {
        this.character = character;
        this.name = character.getName();
        this.x = startX;
        this.y = startY;
        this.angle = startAngle;
        this.maxSpeed = character.getTopSpeed();
        this.acceleration = character.getAcceleration() * 3.5f;
        this.handling = character.getHandling() * 180f; // degrees per second at full speed ratio
        this.speed = 0;
        this.currentLap = 0;
        this.nextWaypoint = 0;
        this.finishedRace = false;
        this.raceTime = 0;
        this.crossedStartLine = false;
    }

    public void update(float delta, Track track) {
        if (finishedRace) return;

        raceTime += delta;
        applyMovement(delta, track);
        checkWaypoints(track.getWaypoints());
        checkStartLine();
    }

    protected void applyMovement(float delta, Track track) {
        // Convert angle to radians for movement
        double rad = Math.toRadians(angle);
        float vx = (float)(speed * Math.cos(rad));
        float vy = (float)(speed * Math.sin(rad));

        float newX = x + vx * delta;
        float newY = y + vy * delta;

        // Off-road check
        boolean onTrack = track.isOnTrack(newX, newY);
        float frictionToUse = onTrack ? friction : offRoadFriction;

        // Apply friction & move
        speed *= frictionToUse;
        x = newX;
        y = newY;

        // Clamp to screen bounds
        x = Math.max(20, Math.min(780, x));
        y = Math.max(20, Math.min(580, y));
    }

    protected void accelerate(float delta) {
        speed = Math.min(speed + acceleration * delta * 60f, maxSpeed);
    }

    protected void brake(float delta) {
        speed = Math.max(speed - acceleration * 1.5f * delta * 60f, -maxSpeed * 0.4f);
    }

    protected void turnLeft(float delta) {
        // Tighter turn at lower speeds (like Mario Kart PC)
        float turnRate = handling * (0.4f + 0.6f * (Math.abs(speed) / maxSpeed));
        angle += turnRate * delta;
    }

    protected void turnRight(float delta) {
        float turnRate = handling * (0.4f + 0.6f * (Math.abs(speed) / maxSpeed));
        angle -= turnRate * delta;
    }

    private void checkWaypoints(ArrayList<TrackWaypoint> waypoints) {
        if (waypoints.isEmpty()) return;
        totalWaypoints = waypoints.size();
        TrackWaypoint wp = waypoints.get(nextWaypoint % totalWaypoints);
        if (wp.contains(x, y)) {
            nextWaypoint++;
        }
    }

    private void checkStartLine() {
        // Start line is vertical line at START_X, between START_Y_MIN and START_Y_MAX
        boolean nearLine = Math.abs(x - START_X) < START_ZONE && y > START_Y_MIN && y < START_Y_MAX;
        if (nearLine && !crossedStartLine && nextWaypoint >= totalWaypoints - 2 && currentLap > 0) {
            currentLap++;
            nextWaypoint = 0;
            crossedStartLine = true;
        } else if (nearLine && !crossedStartLine && currentLap == 0 && nextWaypoint == 0) {
            // First crossing starts lap 1
            currentLap = 1;
            crossedStartLine = true;
        } else if (!nearLine) {
            crossedStartLine = false;
        }
    }

    /**
     * Render the Boohbah body as a colorful circle with details.
     */
    public void render(ShapeRenderer sr) {
        float r = character.getColorR() / 255f;
        float g = character.getColorG() / 255f;
        float b = character.getColorB() / 255f;

        // Shadow
        sr.setColor(0, 0, 0, 0.3f);
        sr.ellipse(x - bodyRadius * 0.9f - 2, y - bodyRadius * 0.6f - 2,
                   bodyRadius * 1.8f, bodyRadius * 1.2f);

        // Body (big round Boohbah shape)
        sr.setColor(r, g, b, 1f);
        sr.circle(x, y, bodyRadius);

        // Belly (lighter circle)
        sr.setColor(Math.min(r + 0.3f, 1f), Math.min(g + 0.3f, 1f), Math.min(b + 0.3f, 1f), 1f);
        sr.circle(x, y - 2, bodyRadius * 0.55f);

        // Eyes
        double rad = Math.toRadians(angle);
        float eyeOff = 7f;
        float ex1 = x + (float)(Math.cos(rad + 0.4) * eyeOff);
        float ey1 = y + (float)(Math.sin(rad + 0.4) * eyeOff);
        float ex2 = x + (float)(Math.cos(rad - 0.4) * eyeOff);
        float ey2 = y + (float)(Math.sin(rad - 0.4) * eyeOff);

        sr.setColor(Color.WHITE);
        sr.circle(ex1, ey1, 3.5f);
        sr.circle(ex2, ey2, 3.5f);
        sr.setColor(Color.BLACK);
        sr.circle(ex1 + (float)(Math.cos(rad) * 1.5f), ey1 + (float)(Math.sin(rad) * 1.5f), 1.8f);
        sr.circle(ex2 + (float)(Math.cos(rad) * 1.5f), ey2 + (float)(Math.sin(rad) * 1.5f), 1.8f);

        // Speed trail
        if (speed > 50) {
            sr.setColor(r, g, b, 0.3f);
            float trailX = x - (float)(Math.cos(rad) * 16);
            float trailY = y - (float)(Math.sin(rad) * 16);
            sr.circle(trailX, trailY, bodyRadius * 0.5f);
        }
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getAngle() { return angle; }
    public float getSpeed() { return speed; }
    public int getCurrentLap() { return currentLap; }
    public int getNextWaypoint() { return nextWaypoint; }
    public boolean isFinished() { return finishedRace; }
    public void setFinished(boolean f) { finishedRace = f; }
    public float getRaceTime() { return raceTime; }
    public int getPosition() { return position; }
    public void setPosition(int p) { position = p; }
    public String getName() { return name; }
    public BoohbahCharacter getCharacter() { return character; }
    public float getMaxSpeed() { return maxSpeed; }

    /**
     * Progress score for position calculation (higher = further in race).
     */
    public float getRaceProgress() {
        return currentLap * 1000f + nextWaypoint * 10f;
    }
}
