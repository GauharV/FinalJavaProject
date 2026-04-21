package com.zingzingracing.racing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.zingzingracing.characters.BoohbahCharacter;
import com.zingzingracing.track.Track;

/**
 * Player-controlled racer. Reads keyboard input.
 * Arrow keys or WASD to drive — just like Mario Kart PC.
 */
public class PlayerRacer extends Racer {

    private boolean countdownDone = false;

    public PlayerRacer(BoohbahCharacter character, float startX, float startY) {
        super(character, startX, startY, 90f); // Start facing up
    }

    public void setCountdownDone(boolean done) {
        this.countdownDone = done;
    }

    @Override
    public void update(float delta, Track track) {
        if (finishedRace) return;
        raceTime += delta;

        if (countdownDone) {
            handleInput(delta);
        }

        applyMovement(delta, track);
        checkWaypointsPublic(track);
        checkStartLinePublic();
    }

    private void handleInput(float delta) {
        boolean up    = Gdx.input.isKeyPressed(Input.Keys.UP)    || Gdx.input.isKeyPressed(Input.Keys.W);
        boolean down  = Gdx.input.isKeyPressed(Input.Keys.DOWN)  || Gdx.input.isKeyPressed(Input.Keys.S);
        boolean left  = Gdx.input.isKeyPressed(Input.Keys.LEFT)  || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        if (up)    accelerate(delta);
        if (down)  brake(delta);
        if (left)  turnLeft(delta);
        if (right) turnRight(delta);
    }

    // Expose protected methods for override pattern
    private void checkWaypointsPublic(Track track) {
        java.util.ArrayList<com.zingzingracing.track.TrackWaypoint> waypoints = track.getWaypoints();
        if (waypoints.isEmpty()) return;
        totalWaypoints = waypoints.size();
        com.zingzingracing.track.TrackWaypoint wp = waypoints.get(nextWaypoint % totalWaypoints);
        if (wp.contains(x, y)) {
            nextWaypoint++;
        }
    }

    private boolean crossedStart = false;
    private void checkStartLinePublic() {
        float startX = Track.TRACK_CENTER_X;
        float startYMin = Track.TRACK_CENTER_Y + Track.INNER_RY;
        float startYMax = Track.TRACK_CENTER_Y + Track.OUTER_RY;
        float zone = 20f;

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
