package zingzingzingbah.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Represents a racing kart on the track.
 * Handles physics simulation, player input, and AI waypoint following.
 *
 * Java 2 concepts demonstrated:
 *   - Object composition (Character inside Kart)
 *   - Encapsulation with setters/getters for input state
 */
public class Kart {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------
    private static final double KART_W        = 18;
    private static final double KART_H        = 26;
    private static final double ON_TRACK_FRICTION  = 0.995;  // minimal drag
    private static final double OFF_TRACK_FRICTION = 0.940;  // grass slows you way down
    private static final double TURN_BASE     = 0.038;       // radians per frame scaling

    // -----------------------------------------------------------------------
    // Identity
    // -----------------------------------------------------------------------
    private final Character character;
    private final boolean isPlayer;

    // -----------------------------------------------------------------------
    // Physics state
    // -----------------------------------------------------------------------
    private double x, y;
    private double angle;   // radians; 0 = right, PI/2 = down
    private double speed;   // pixels per second

    // -----------------------------------------------------------------------
    // Race state
    // -----------------------------------------------------------------------
    private int lap            = 0;
    private int nextCheckpoint = 1;  // skip checkpoint 0 at race start (it's the finish line)
    private int racePosition   = 1;
    private boolean finished   = false;
    private boolean onTrack    = true;

    // -----------------------------------------------------------------------
    // Player input flags
    // -----------------------------------------------------------------------
    private boolean accelerating, braking, turningLeft, turningRight;

    // -----------------------------------------------------------------------
    // AI state
    // -----------------------------------------------------------------------
    private int targetWaypoint = 1;
    private final double aiSpeedMultiplier;  // small variation per AI kart

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    public Kart(Character character, double startX, double startY,
                double startAngle, boolean isPlayer, double aiSpeedMult) {
        this.character        = character;
        this.x                = startX;
        this.y                = startY;
        this.angle            = startAngle;
        this.isPlayer         = isPlayer;
        this.aiSpeedMultiplier = aiSpeedMult;
    }

    // -----------------------------------------------------------------------
    // Update (called every game frame)
    // -----------------------------------------------------------------------
    /**
     * Updates kart physics for one time step.
     * @param delta       seconds since last frame
     * @param waypointX   track waypoint X positions (for AI)
     * @param waypointY   track waypoint Y positions (for AI)
     */
    public void update(double delta, double[] waypointX, double[] waypointY) {
        if (finished) return;

        double topSpeed     = character.getTopSpeed();
        double accel        = character.getAcceleration();
        double handling     = character.getHandling();

        if (isPlayer) {
            updatePlayerInput(topSpeed, accel, handling, delta);
        } else {
            updateAI(topSpeed, accel, handling, delta, waypointX, waypointY);
        }

        // Apply appropriate friction
        double friction = onTrack ? ON_TRACK_FRICTION : OFF_TRACK_FRICTION;
        speed *= Math.pow(friction, delta * 60.0);

        // Clamp speed
        speed = Math.max(0, Math.min(speed, topSpeed));

        // Move kart
        x += Math.cos(angle) * speed * delta;
        y += Math.sin(angle) * speed * delta;

        // Keep kart within canvas bounds
        x = Math.max(10, Math.min(890, x));
        y = Math.max(10, Math.min(590, y));
    }

    private void updatePlayerInput(double topSpeed, double accel, double handling, double delta) {
        // Turn: scaled by current speed so you can't spin at standstill
        if (speed > 8) {
            double turnFactor = handling * TURN_BASE * Math.min(speed / 120.0, 1.0);
            if (turningLeft)  angle -= turnFactor;
            if (turningRight) angle += turnFactor;
        }

        if (accelerating) {
            speed += accel * delta;
        }
        if (braking) {
            speed -= accel * 1.6 * delta;
            speed = Math.max(0, speed);
        }
    }

    private void updateAI(double topSpeed, double accel, double handling, double delta,
                           double[] waypointX, double[] waypointY) {
        // Steer toward target waypoint
        double tx = waypointX[targetWaypoint];
        double ty = waypointY[targetWaypoint];

        double desiredAngle = Math.atan2(ty - y, tx - x);
        double diff = desiredAngle - angle;

        // Normalize diff to [-PI, PI]
        while (diff >  Math.PI) diff -= 2 * Math.PI;
        while (diff < -Math.PI) diff += 2 * Math.PI;

        double turnFactor = handling * TURN_BASE * Math.min(speed / 120.0, 1.0);
        double steer = Math.max(-turnFactor, Math.min(turnFactor, diff));
        angle += steer;

        // Always accelerate (with per-kart speed variation)
        double effectiveTop = topSpeed * aiSpeedMultiplier;
        if (speed < effectiveTop) {
            speed += accel * delta * 0.88;
        }

        // Advance to next waypoint when close enough
        double dist = Math.hypot(tx - x, ty - y);
        if (dist < 50) {
            targetWaypoint = (targetWaypoint + 1) % waypointX.length;
        }
    }

    // -----------------------------------------------------------------------
    // Drawing
    // -----------------------------------------------------------------------
    /**
     * Draws the kart on the given GraphicsContext.
     * Origin is at (x, y), kart is rotated to match heading angle.
     */
    public void draw(GraphicsContext gc) {
        gc.save();
        gc.translate(x, y);
        // +90 because kart sprite faces "up" (−Y) but angle=0 means moving right (+X)
        gc.rotate(Math.toDegrees(angle) + 90.0);

        // Wheels (behind body)
        gc.setFill(Color.web("#1A1A1A"));
        double wx = KART_W / 2 + 2;
        gc.fillRoundRect(-wx - 2, -KART_H / 2 + 2,  5, 7, 2, 2);  // front-left
        gc.fillRoundRect( wx - 3, -KART_H / 2 + 2,  5, 7, 2, 2);  // front-right
        gc.fillRoundRect(-wx - 2,  KART_H / 2 - 9,  5, 7, 2, 2);  // rear-left
        gc.fillRoundRect( wx - 3,  KART_H / 2 - 9,  5, 7, 2, 2);  // rear-right

        // Kart body
        gc.setFill(character.getBodyColor());
        gc.fillRoundRect(-KART_W / 2, -KART_H / 2, KART_W, KART_H, 6, 6);

        // Windshield (accent color at front)
        gc.setFill(character.getAccentColor());
        gc.fillRoundRect(-KART_W / 2 + 3, -KART_H / 2 + 3, KART_W - 6, KART_H / 2 - 3, 3, 3);

        // Highlight
        gc.setFill(Color.color(1, 1, 1, 0.25));
        gc.fillRoundRect(-KART_W / 2 + 4, -KART_H / 2 + 4, KART_W / 2 - 2, 5, 2, 2);

        gc.restore();

        // Name initial above kart (drawn in world space, no rotation)
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(character.getName().substring(0, 2).toUpperCase(), x, y - KART_H / 2 - 4);
    }

    // -----------------------------------------------------------------------
    // Player input setters
    // -----------------------------------------------------------------------
    public void setAccelerating(boolean v) { accelerating = v; }
    public void setBraking(boolean v)      { braking = v; }
    public void setTurningLeft(boolean v)  { turningLeft = v; }
    public void setTurningRight(boolean v) { turningRight = v; }

    // -----------------------------------------------------------------------
    // State getters / setters
    // -----------------------------------------------------------------------
    public double getX()                 { return x; }
    public double getY()                 { return y; }
    public double getAngle()             { return angle; }
    public double getSpeed()             { return speed; }
    public int getLap()                  { return lap; }
    public int getNextCheckpoint()       { return nextCheckpoint; }
    public int getRacePosition()         { return racePosition; }
    public boolean isFinished()          { return finished; }
    public boolean isPlayer()            { return isPlayer; }
    public Character getCharacter()      { return character; }
    public int getTargetWaypoint()       { return targetWaypoint; }

    public void setLap(int lap)                  { this.lap = lap; }
    public void setNextCheckpoint(int cp)        { this.nextCheckpoint = cp; }
    public void setRacePosition(int pos)         { this.racePosition = pos; }
    public void setFinished(boolean finished)    { this.finished = finished; }
    public void setOnTrack(boolean onTrack)      { this.onTrack = onTrack; }
    public void setSpeed(double speed)           { this.speed = speed; }
}
