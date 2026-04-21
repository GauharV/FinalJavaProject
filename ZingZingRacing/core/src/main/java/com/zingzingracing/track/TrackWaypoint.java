package com.zingzingracing.track;

import com.badlogic.gdx.math.Vector2;

/**
 * A single waypoint on the track used for AI pathfinding and lap counting.
 */
public class TrackWaypoint {
    public final Vector2 position;
    public final float radius;
    public final int index;

    public TrackWaypoint(float x, float y, float radius, int index) {
        this.position = new Vector2(x, y);
        this.radius = radius;
        this.index = index;
    }

    public boolean contains(float x, float y) {
        return position.dst(x, y) <= radius;
    }
}
