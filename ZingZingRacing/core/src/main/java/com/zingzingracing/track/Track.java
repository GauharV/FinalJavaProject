package com.zingzingracing.track;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

/**
 * Defines a oval/circuit track drawn procedurally.
 * Uses ArrayList<TrackWaypoint> (Java 2 concept - generics + lists).
 */
public class Track {

    // Track bounds (oval shape)
    public static final float TRACK_CENTER_X = 400f;
    public static final float TRACK_CENTER_Y = 300f;
    public static final float OUTER_RX = 340f;
    public static final float OUTER_RY = 240f;
    public static final float INNER_RX = 200f;
    public static final float INNER_RY = 130f;
    public static final float TRACK_WIDTH = 100f;

    // Start/finish line
    public static final float START_X = 400f;
    public static final float START_Y = 530f;

    private final ArrayList<TrackWaypoint> waypoints;

    public Track() {
        waypoints = new ArrayList<>();
        buildWaypoints();
    }

    private void buildWaypoints() {
        // Place waypoints around the oval
        int numPoints = 16;
        for (int i = 0; i < numPoints; i++) {
            double angle = (2 * Math.PI * i / numPoints) - Math.PI / 2;
            float rx = (OUTER_RX + INNER_RX) / 2f;
            float ry = (OUTER_RY + INNER_RY) / 2f;
            float wx = TRACK_CENTER_X + (float)(rx * Math.cos(angle));
            float wy = TRACK_CENTER_Y + (float)(ry * Math.sin(angle));
            waypoints.add(new TrackWaypoint(wx, wy, 55f, i));
        }
    }

    public ArrayList<TrackWaypoint> getWaypoints() {
        return waypoints;
    }

    /**
     * Check if a position is on the track surface.
     */
    public boolean isOnTrack(float x, float y) {
        float dx = (x - TRACK_CENTER_X);
        float dy = (y - TRACK_CENTER_Y);

        // Normalized ellipse check
        float outerDist = (dx * dx) / (OUTER_RX * OUTER_RX) + (dy * dy) / (OUTER_RY * OUTER_RY);
        float innerDist = (dx * dx) / (INNER_RX * INNER_RX) + (dy * dy) / (INNER_RY * INNER_RY);

        return outerDist <= 1.0f && innerDist >= 1.0f;
    }

    /**
     * Draw the track using ShapeRenderer.
     */
    public void render(ShapeRenderer sr) {
        // Draw road (dark gray) - filled oval between outer and inner
        int segments = 120;

        // Draw outer oval filled (road color)
        sr.setColor(0.25f, 0.25f, 0.25f, 1f);
        sr.ellipse(TRACK_CENTER_X - OUTER_RX, TRACK_CENTER_Y - OUTER_RY,
                   OUTER_RX * 2, OUTER_RY * 2, segments);

        // Draw inner oval (grass) to cut out center
        sr.setColor(0.2f, 0.55f, 0.15f, 1f);
        sr.ellipse(TRACK_CENTER_X - INNER_RX, TRACK_CENTER_Y - INNER_RY,
                   INNER_RX * 2, INNER_RY * 2, segments);

        // Draw track border lines (white)
        sr.setColor(Color.WHITE);
        drawEllipseOutline(sr, TRACK_CENTER_X, TRACK_CENTER_Y, OUTER_RX, OUTER_RY, segments, 3f);
        drawEllipseOutline(sr, TRACK_CENTER_X, TRACK_CENTER_Y, INNER_RX, INNER_RY, segments, 3f);

        // Start/finish line
        sr.setColor(Color.WHITE);
        sr.rectLine(TRACK_CENTER_X - 3, TRACK_CENTER_Y + INNER_RY,
                    TRACK_CENTER_X - 3, TRACK_CENTER_Y + OUTER_RY, 5f);

        // Dashed center line
        sr.setColor(0.9f, 0.9f, 0.0f, 0.5f);
        float midRX = (OUTER_RX + INNER_RX) / 2f;
        float midRY = (OUTER_RY + INNER_RY) / 2f;
        for (int i = 0; i < segments; i += 2) {
            double a1 = (2 * Math.PI * i / segments);
            double a2 = (2 * Math.PI * (i + 1) / segments);
            float x1 = TRACK_CENTER_X + (float)(midRX * Math.cos(a1));
            float y1 = TRACK_CENTER_Y + (float)(midRY * Math.sin(a1));
            float x2 = TRACK_CENTER_X + (float)(midRX * Math.cos(a2));
            float y2 = TRACK_CENTER_Y + (float)(midRY * Math.sin(a2));
            sr.rectLine(x1, y1, x2, y2, 2f);
        }
    }

    private void drawEllipseOutline(ShapeRenderer sr, float cx, float cy, float rx, float ry, int segs, float thickness) {
        for (int i = 0; i < segs; i++) {
            double a1 = (2 * Math.PI * i / segs);
            double a2 = (2 * Math.PI * (i + 1) / segs);
            float x1 = cx + (float)(rx * Math.cos(a1));
            float y1 = cy + (float)(ry * Math.sin(a1));
            float x2 = cx + (float)(rx * Math.cos(a2));
            float y2 = cy + (float)(ry * Math.sin(a2));
            sr.rectLine(x1, y1, x2, y2, thickness);
        }
    }

    /**
     * Draw background grass.
     */
    public void renderBackground(ShapeRenderer sr) {
        sr.setColor(0.2f, 0.55f, 0.15f, 1f);
        sr.rect(0, 0, 800, 600);
    }

    public Vector2 getStartPosition(int laneIndex) {
        // Stagger start positions
        float offset = laneIndex * 30f - 60f;
        return new Vector2(TRACK_CENTER_X + offset, TRACK_CENTER_Y + INNER_RY + 30f);
    }
}
