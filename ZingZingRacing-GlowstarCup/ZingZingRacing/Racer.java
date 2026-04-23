import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Represents one racer on the track.
 */
public class Racer {
    public static final int TOTAL_LAPS = 3;

    private double x;
    private double y;
    private double angle;
    private double speed;

    private int currentWaypointIndex;
    private int lap;
    private boolean finished;
    private int finishPosition;

    private final BoohbahCharacter character;
    private final boolean isPlayer;
    private final Queue<Integer> checkpointQueue;

    private int abilityTimerFrames;
    private int abilityCooldownFrames;

    public Racer(BoohbahCharacter character,
                 double startX, double startY,
                 double startAngle, boolean isPlayer) {
        this.character = character;
        this.x = startX;
        this.y = startY;
        this.angle = startAngle;
        this.speed = 0;
        this.currentWaypointIndex = 1;
        this.lap = 1;
        this.finished = false;
        this.finishPosition = 0;
        this.isPlayer = isPlayer;
        this.checkpointQueue = new ArrayDeque<Integer>();
        this.abilityTimerFrames = 0;
        this.abilityCooldownFrames = 60;
    }

    public void draw(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        final int radius = 18;

        if (isAbilityActive()) {
            Color glow = character.getAbilityAccent();
            int pulse = 12 + (abilityTimerFrames % 18);
            g2d.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 70));
            g2d.fillOval((int) x - radius - pulse / 2, (int) y - radius - pulse / 2,
                         radius * 2 + pulse, radius * 2 + pulse);
        }

        g2d.setColor(new Color(0, 0, 0, 70));
        g2d.fillOval((int) x - radius + 4, (int) y - radius + 5, radius * 2, radius * 2);

        g2d.setColor(character.getColor());
        g2d.fillOval((int) x - radius, (int) y - radius, radius * 2, radius * 2);

        g2d.setColor(new Color(255, 255, 255, 75));
        g2d.fillOval((int) x - radius + 5, (int) y - radius + 5, radius - 4, radius - 4);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval((int) x - radius, (int) y - radius, radius * 2, radius * 2);

        double eyeOffset = radius * 0.45;
        double perpAngle = angle + Math.PI / 2.0;
        int eyeBaseX = (int) (x + Math.cos(angle) * (radius * 0.28));
        int eyeBaseY = (int) (y + Math.sin(angle) * (radius * 0.28));

        for (int side : new int[]{-1, 1}) {
            int ex = (int) (eyeBaseX + Math.cos(perpAngle) * eyeOffset * side);
            int ey = (int) (eyeBaseY + Math.sin(perpAngle) * eyeOffset * side);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(ex - 5, ey - 5, 10, 12);
            g2d.setColor(new Color(20, 20, 20));
            g2d.fillOval(ex - 3, ey - 3, 6, 8);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(ex, ey - 1, 2, 2);
        }

        int noseX = (int) (x + Math.cos(angle) * (radius + 4));
        int noseY = (int) (y + Math.sin(angle) * (radius + 4));
        g2d.setColor(Color.WHITE);
        g2d.fillOval(noseX - 3, noseY - 3, 7, 7);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawOval(noseX - 3, noseY - 3, 7, 7);

        if (isPlayer) {
            g2d.setColor(new Color(255, 255, 255, 210));
            g2d.setStroke(new BasicStroke(2.4f));
            g2d.drawOval((int) x - radius - 6, (int) y - radius - 6, radius * 2 + 12, radius * 2 + 12);
        }
    }

    public void tickAbilityState() {
        if (abilityTimerFrames > 0) {
            abilityTimerFrames--;
        }
        if (abilityCooldownFrames > 0) {
            abilityCooldownFrames--;
        }
    }

    public boolean canActivateAbility() {
        return !finished && abilityTimerFrames == 0 && abilityCooldownFrames == 0;
    }

    public void activateAbility() {
        abilityTimerFrames = character.getAbilityDurationFrames();
        abilityCooldownFrames = character.getAbilityCooldownFrames();
    }

    public boolean isAbilityActive() { return abilityTimerFrames > 0; }
    public int getAbilityTimerFrames() { return abilityTimerFrames; }
    public int getAbilityCooldownFrames() { return abilityCooldownFrames; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngle() { return angle; }
    public double getSpeed() { return speed; }
    public int getLap() { return lap; }
    public int getCurrentWaypointIndex() { return currentWaypointIndex; }
    public boolean isPlayer() { return isPlayer; }
    public boolean isFinished() { return finished; }
    public int getFinishPosition() { return finishPosition; }
    public BoohbahCharacter getCharacter() { return character; }
    public Queue<Integer> getCheckpointQueue() { return checkpointQueue; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setAngle(double angle) { this.angle = angle; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setCurrentWaypointIndex(int i) { this.currentWaypointIndex = i; }
    public void incrementLap() { this.lap++; }
    public void setFinished(boolean f) { this.finished = f; }
    public void setFinishPosition(int p) { this.finishPosition = p; }
}
