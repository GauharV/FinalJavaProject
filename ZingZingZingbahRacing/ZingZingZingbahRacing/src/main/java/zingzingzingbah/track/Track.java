package zingzingzingbah.track;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * Defines the oval racing track: boundaries, waypoints, and rendering.
 *
 * The track is an elliptical donut shape:
 *   - Outer oval: semi-axes (OUTER_RX, OUTER_RY)
 *   - Inner oval: semi-axes (INNER_RX, INNER_RY)
 *   - A kart is ON the track if inside outer AND outside inner.
 *
 * Java 2 concepts demonstrated:
 *   - Constants (static final)
 *   - Arrays for waypoint data
 */
public class Track {

    // -----------------------------------------------------------------------
    // Track geometry
    // -----------------------------------------------------------------------
    public static final double CENTER_X  = 450;
    public static final double CENTER_Y  = 300;

    public static final double OUTER_RX  = 390;
    public static final double OUTER_RY  = 250;
    public static final double INNER_RX  = 220;
    public static final double INNER_RY  = 120;

    // Midline oval (used for waypoints and dashed center line)
    private static final double MID_RX   = (OUTER_RX + INNER_RX) / 2.0;  // 305
    private static final double MID_RY   = (OUTER_RY + INNER_RY) / 2.0;  // 185

    // Checkpoint proximity radius
    private static final double CP_RADIUS = 68;

    // Total checkpoints evenly distributed around the oval
    public static final int NUM_CHECKPOINTS = 8;

    // -----------------------------------------------------------------------
    // Waypoints (ArrayList would work but a plain array is sufficient here)
    // -----------------------------------------------------------------------
    private final double[] waypointX;
    private final double[] waypointY;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    public Track() {
        waypointX = new double[NUM_CHECKPOINTS];
        waypointY = new double[NUM_CHECKPOINTS];

        // Checkpoint 0 is at the RIGHT side of the oval (angle = 0).
        // Clockwise order: 0 → 1 → 2 → ... → 7 → (back to 0)
        for (int i = 0; i < NUM_CHECKPOINTS; i++) {
            double theta = (2.0 * Math.PI * i) / NUM_CHECKPOINTS;  // 0, π/4, π/2, ...
            waypointX[i] = CENTER_X + MID_RX * Math.cos(theta);
            waypointY[i] = CENTER_Y + MID_RY * Math.sin(theta);
        }
    }

    // -----------------------------------------------------------------------
    // Track boundary check
    // -----------------------------------------------------------------------
    /**
     * @return true if the point (x, y) lies on the track surface.
     */
    public boolean isOnTrack(double x, double y) {
        double outerVal = Math.pow((x - CENTER_X) / OUTER_RX, 2)
                        + Math.pow((y - CENTER_Y) / OUTER_RY, 2);
        double innerVal = Math.pow((x - CENTER_X) / INNER_RX, 2)
                        + Math.pow((y - CENTER_Y) / INNER_RY, 2);
        return (outerVal <= 1.0) && (innerVal >= 1.0);
    }

    /**
     * @return true if the point (x, y) is within checkpoint radius of waypoint[index].
     */
    public boolean isAtCheckpoint(double x, double y, int index) {
        return Math.hypot(x - waypointX[index], y - waypointY[index]) < CP_RADIUS;
    }

    // -----------------------------------------------------------------------
    // Starting grid positions (near waypoint 0 on the right side)
    // -----------------------------------------------------------------------
    /** Returns 5 staggered X start positions. */
    public double[] getStartX() {
        return new double[]{ 755, 755, 720, 720, 685 };
    }

    /** Returns 5 staggered Y start positions. */
    public double[] getStartY() {
        return new double[]{ 270, 335, 255, 350, 300 };
    }

    /** Initial heading angle: pointing downward (clockwise on right side of oval). */
    public double getStartAngle() {
        return Math.PI / 2.0;   // PI/2 = pointing in +Y direction = down = clockwise
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------
    public double[] getWaypointX() { return waypointX; }
    public double[] getWaypointY() { return waypointY; }

    // -----------------------------------------------------------------------
    // Drawing
    // -----------------------------------------------------------------------
    /**
     * Draws the full track onto the given GraphicsContext.
     */
    public void draw(GraphicsContext gc) {
        // --- Outer grass background (already filled by RaceScreen) ---
        // We draw here only the track oval + inner grass + decorations

        // Track surface (dark asphalt)
        gc.setFill(Color.web("#3C3C3C"));
        gc.fillOval(CENTER_X - OUTER_RX, CENTER_Y - OUTER_RY,
                    OUTER_RX * 2, OUTER_RY * 2);

        // Inner grass island
        LinearGradient innerGrass = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#3DB84E")),
                new Stop(1, Color.web("#2E8F3C")));
        gc.setFill(innerGrass);
        gc.fillOval(CENTER_X - INNER_RX, CENTER_Y - INNER_RY,
                    INNER_RX * 2, INNER_RY * 2);

        // Inner grass decorations: colorful Boohbah-style polka dots
        drawInnerDecorations(gc);

        // Outer white boundary line
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3.5);
        gc.strokeOval(CENTER_X - OUTER_RX, CENTER_Y - OUTER_RY,
                      OUTER_RX * 2, OUTER_RY * 2);

        // Inner white boundary line
        gc.strokeOval(CENTER_X - INNER_RX, CENTER_Y - INNER_RY,
                      INNER_RX * 2, INNER_RY * 2);

        // Dashed center line
        gc.setStroke(Color.color(1, 1, 0, 0.45));
        gc.setLineWidth(2);
        gc.setLineDashes(14, 9);
        gc.strokeOval(CENTER_X - MID_RX, CENTER_Y - MID_RY,
                      MID_RX * 2, MID_RY * 2);
        gc.setLineDashes((double[]) null);

        // Alternating red/white curb dots along boundaries
        drawCurbs(gc);

        // Start / finish line (checkerboard at waypoint 0 — right side of oval)
        drawStartFinishLine(gc);
    }

    private void drawInnerDecorations(GraphicsContext gc) {
        // Stars pattern inside inner oval
        java.util.Random rng = new java.util.Random(7);
        Color[] starColors = {
            Color.web("#FFD700"), Color.web("#FF69B4"),
            Color.web("#00CFFF"), Color.web("#FF6600")
        };
        for (int i = 0; i < 22; i++) {
            double theta = rng.nextDouble() * 2 * Math.PI;
            double r     = rng.nextDouble() * 0.75;  // 0..0.75 of inner oval
            double px    = CENTER_X + INNER_RX * r * Math.cos(theta);
            double py    = CENTER_Y + INNER_RY * r * Math.sin(theta);
            double sz    = 4 + rng.nextDouble() * 10;
            gc.setFill(Color.color(
                    starColors[i % starColors.length].getRed(),
                    starColors[i % starColors.length].getGreen(),
                    starColors[i % starColors.length].getBlue(),
                    0.45));
            gc.fillOval(px - sz / 2, py - sz / 2, sz, sz);
        }
    }

    private void drawCurbs(GraphicsContext gc) {
        int segments = 60;
        for (int i = 0; i < segments; i++) {
            double theta = (2.0 * Math.PI * i) / segments;
            gc.setFill(i % 2 == 0 ? Color.RED : Color.WHITE);

            // Outer curb (just outside outer boundary)
            double ox = CENTER_X + (OUTER_RX + 6) * Math.cos(theta);
            double oy = CENTER_Y + (OUTER_RY + 6) * Math.sin(theta);
            gc.fillOval(ox - 4, oy - 4, 8, 8);

            // Inner curb (just inside inner boundary)
            double ix = CENTER_X + (INNER_RX - 6) * Math.cos(theta);
            double iy = CENTER_Y + (INNER_RY - 6) * Math.sin(theta);
            gc.fillOval(ix - 4, iy - 4, 8, 8);
        }
    }

    private void drawStartFinishLine(GraphicsContext gc) {
        // Waypoint 0 is at (CENTER_X + MID_RX, CENTER_Y) ≈ (755, 300)
        // The track runs vertically here, so the line is horizontal (up-down).
        double lineX = waypointX[0];  // ≈ 755

        // The line spans from the inner oval to the outer oval at this X.
        // Outer oval at lineX: solve for Y. (lineX - CENTER_X)²/OUTER_RX² + (y - CENTER_Y)²/OUTER_RY² = 1
        double outerFrac = Math.pow((lineX - CENTER_X) / OUTER_RX, 2);
        double outerDY   = OUTER_RY * Math.sqrt(Math.max(0, 1 - outerFrac));

        // At x ≈ 755, the inner oval x-extent ends at CENTER_X + INNER_RX ≈ 670,
        // so the entire column is outside the inner oval — the line spans the full track width here.
        double y1 = CENTER_Y - outerDY;
        double y2 = CENTER_Y + outerDY;

        int tiles  = 10;
        double tH  = (y2 - y1) / tiles;
        for (int i = 0; i < tiles; i++) {
            gc.setFill(i % 2 == 0 ? Color.WHITE : Color.BLACK);
            gc.fillRect(lineX - 6, y1 + i * tH, 12, tH + 1);
        }
    }
}
