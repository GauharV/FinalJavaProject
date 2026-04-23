import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/**
 * A scalable track with multiple cup circuits.
 */
public class Track {

    public enum Layout {
        SKY_RIBBON("Sky Ribbon", "A fast opener above the clouds"),
        MOON_GARDEN("Moon Garden", "Silver hedges and moonlit sweepers"),
        SUNSET_HARBOR("Sunset Harbor", "Docks, water, and a long run home");

        private final String displayName;
        private final String subtitle;

        Layout(String displayName, String subtitle) {
            this.displayName = displayName;
            this.subtitle = subtitle;
        }

        public String getDisplayName() { return displayName; }
        public String getSubtitle() { return subtitle; }
    }

    private static final class BoostStrip {
        private final int startIndex;
        private final int endIndex;
        private final double startT;
        private final int count;

        private BoostStrip(int startIndex, int endIndex, double startT, int count) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.startT = startT;
            this.count = count;
        }
    }

    private static final int WORLD_WIDTH = 1800;
    private static final int WORLD_HEIGHT = 1000;

    public static final double TRACK_WIDTH = 128.0;
    public static final double WAYPOINT_RADIUS = 58.0;

    private final Layout layout;
    private final List<double[]> waypoints;
    private final List<BoostStrip> boostStrips;

    public Track(Layout layout) {
        this.layout = layout == null ? Layout.SKY_RIBBON : layout;
        this.waypoints = new ArrayList<double[]>();
        this.boostStrips = new ArrayList<BoostStrip>();
        buildLayout();
    }

    public Layout getLayout() { return layout; }
    public String getName() { return layout.getDisplayName(); }
    public String getSubtitle() { return layout.getSubtitle(); }
    public List<double[]> getWaypoints() { return waypoints; }
    public int getWaypointCount() { return waypoints.size(); }
    public double getWaypointRadius() { return WAYPOINT_RADIUS; }
    public double getTrackWidth() { return TRACK_WIDTH; }
    public int getWorldWidth() { return WORLD_WIDTH; }
    public int getWorldHeight() { return WORLD_HEIGHT; }

    public double[] getWaypoint(int index) {
        return waypoints.get(index % waypoints.size());
    }

    public boolean hasCrossedWaypointGate(double prevX, double prevY,
                                          double nextX, double nextY,
                                          int waypointIndex) {
        double[] waypoint = getWaypoint(waypointIndex);
        double[] tangent = getWaypointTangent(waypointIndex);
        double gateNormalX = -tangent[1];
        double gateNormalY = tangent[0];

        double alongBefore = dot(prevX - waypoint[0], prevY - waypoint[1], tangent[0], tangent[1]);
        double alongAfter = dot(nextX - waypoint[0], nextY - waypoint[1], tangent[0], tangent[1]);
        if (alongBefore >= 0.0 || alongAfter < 0.0) {
            return false;
        }

        double denom = alongAfter - alongBefore;
        double travelT = denom == 0.0 ? 1.0 : (-alongBefore / denom);
        travelT = Math.max(0.0, Math.min(1.0, travelT));

        double crossX = prevX + (nextX - prevX) * travelT;
        double crossY = prevY + (nextY - prevY) * travelT;
        double lateral = dot(crossX - waypoint[0], crossY - waypoint[1], gateNormalX, gateNormalY);

        return Math.abs(lateral) <= TRACK_WIDTH * 0.58 && isOnTrack(crossX, crossY);
    }

    public double getStartAngle() {
        double[] start = waypoints.get(0);
        double[] next = waypoints.get(1);
        return Math.atan2(next[1] - start[1], next[0] - start[0]);
    }

    public double[] getStartPosition(int slot) {
        double[] start = waypoints.get(0);
        double angle = getStartAngle();
        double dirX = Math.cos(angle);
        double dirY = Math.sin(angle);
        double perpX = -dirY;
        double perpY = dirX;

        double[] laneOffsets = {-20, 20, -20, 20, 0};
        double[] rowOffsets = {42, 42, 94, 94, 146};
        int safeSlot = Math.max(0, Math.min(slot, laneOffsets.length - 1));

        double x = start[0] - dirX * rowOffsets[safeSlot] + perpX * laneOffsets[safeSlot];
        double y = start[1] - dirY * rowOffsets[safeSlot] + perpY * laneOffsets[safeSlot];
        return new double[]{x, y};
    }

    public boolean isOnTrack(double px, double py) {
        int count = waypoints.size();
        for (int i = 0; i < count; i++) {
            double[] a = waypoints.get(i);
            double[] b = waypoints.get((i + 1) % count);
            if (distToSegment(px, py, a[0], a[1], b[0], b[1]) < TRACK_WIDTH / 2.0) {
                return true;
            }
        }
        return false;
    }

    public void draw(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackdrop(g2d);

        GeneralPath centerPath = buildPath();
        Stroke shoulderStroke = new BasicStroke((float) (TRACK_WIDTH + 30),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        Stroke rimStroke = new BasicStroke((float) (TRACK_WIDTH + 12),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        Stroke roadStroke = new BasicStroke((float) TRACK_WIDTH,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        Shape shoulder = shoulderStroke.createStrokedShape(centerPath);
        Shape road = roadStroke.createStrokedShape(centerPath);

        g2d.setColor(getShoulderColor());
        g2d.fill(shoulder);

        g2d.setColor(getRimGlowColor());
        g2d.setStroke(rimStroke);
        g2d.draw(centerPath);

        Paint roadPaint = new GradientPaint(0, 120, getRoadStartColor(),
                                            WORLD_WIDTH, WORLD_HEIGHT, getRoadEndColor());
        g2d.setPaint(roadPaint);
        g2d.fill(road);

        g2d.setColor(new Color(232, 240, 255));
        g2d.setStroke(new BasicStroke((float) (TRACK_WIDTH + 6),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(centerPath);

        g2d.setPaint(roadPaint);
        g2d.setStroke(roadStroke);
        g2d.draw(centerPath);

        g2d.setColor(new Color(255, 255, 255, 26));
        g2d.setStroke(new BasicStroke((float) (TRACK_WIDTH - 34),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(centerPath);

        g2d.setColor(getLaneColor());
        float[] laneDash = {30f, 24f};
        g2d.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 10f, laneDash, 0f));
        g2d.draw(centerPath);

        drawBoostPanels(g2d);
        drawStartFinish(g2d);
    }

    private void buildLayout() {
        waypoints.clear();
        boostStrips.clear();

        switch (layout) {
            case MOON_GARDEN:
                buildMoonGardenCircuit();
                break;
            case SUNSET_HARBOR:
                buildSunsetHarborCircuit();
                break;
            case SKY_RIBBON:
            default:
                buildSkyRibbonCircuit();
                break;
        }
    }

    private void buildSkyRibbonCircuit() {
        addWaypoint(420, 210);
        addWaypoint(650, 170);
        addWaypoint(920, 150);
        addWaypoint(1185, 180);
        addWaypoint(1420, 285);
        addWaypoint(1570, 445);
        addWaypoint(1610, 635);
        addWaypoint(1525, 805);
        addWaypoint(1360, 910);
        addWaypoint(1110, 948);
        addWaypoint(875, 920);
        addWaypoint(705, 830);
        addWaypoint(620, 690);
        addWaypoint(650, 555);
        addWaypoint(795, 470);
        addWaypoint(1010, 450);
        addWaypoint(1205, 510);
        addWaypoint(1265, 645);
        addWaypoint(1195, 760);
        addWaypoint(1030, 805);
        addWaypoint(860, 760);
        addWaypoint(690, 705);
        addWaypoint(520, 770);
        addWaypoint(335, 825);
        addWaypoint(205, 730);
        addWaypoint(170, 545);
        addWaypoint(225, 390);
        addWaypoint(305, 285);

        addBoost(2, 3, 0.28, 4);
        addBoost(8, 9, 0.52, 4);
        addBoost(14, 15, 0.42, 3);
    }

    private void buildMoonGardenCircuit() {
        addWaypoint(460, 170);
        addWaypoint(710, 140);
        addWaypoint(980, 160);
        addWaypoint(1230, 240);
        addWaypoint(1420, 390);
        addWaypoint(1500, 600);
        addWaypoint(1450, 800);
        addWaypoint(1280, 930);
        addWaypoint(1030, 970);
        addWaypoint(780, 950);
        addWaypoint(560, 870);
        addWaypoint(380, 740);
        addWaypoint(290, 560);
        addWaypoint(280, 350);
        addWaypoint(360, 210);
        addWaypoint(520, 130);
        addWaypoint(720, 170);
        addWaypoint(900, 280);
        addWaypoint(1040, 430);
        addWaypoint(1190, 520);
        addWaypoint(1340, 490);
        addWaypoint(1380, 360);
        addWaypoint(1270, 250);
        addWaypoint(1070, 210);
        addWaypoint(840, 240);
        addWaypoint(680, 350);
        addWaypoint(610, 520);
        addWaypoint(660, 690);
        addWaypoint(820, 820);
        addWaypoint(1060, 850);
        addWaypoint(1240, 780);
        addWaypoint(1320, 660);
        addWaypoint(1290, 560);
        addWaypoint(1140, 620);
        addWaypoint(920, 640);
        addWaypoint(760, 580);
        addWaypoint(700, 450);
        addWaypoint(740, 320);

        addBoost(4, 5, 0.32, 3);
        addBoost(11, 12, 0.40, 3);
        addBoost(28, 29, 0.34, 4);
    }

    private void buildSunsetHarborCircuit() {
        addWaypoint(320, 230);
        addWaypoint(610, 190);
        addWaypoint(950, 180);
        addWaypoint(1270, 210);
        addWaypoint(1490, 310);
        addWaypoint(1580, 490);
        addWaypoint(1560, 710);
        addWaypoint(1460, 860);
        addWaypoint(1230, 940);
        addWaypoint(930, 950);
        addWaypoint(700, 900);
        addWaypoint(530, 810);
        addWaypoint(450, 690);
        addWaypoint(470, 570);
        addWaypoint(650, 510);
        addWaypoint(940, 500);
        addWaypoint(1220, 540);
        addWaypoint(1360, 650);
        addWaypoint(1330, 780);
        addWaypoint(1180, 840);
        addWaypoint(980, 820);
        addWaypoint(830, 760);
        addWaypoint(760, 650);
        addWaypoint(820, 560);
        addWaypoint(1030, 450);
        addWaypoint(1310, 390);
        addWaypoint(1450, 280);
        addWaypoint(1410, 170);
        addWaypoint(1150, 110);
        addWaypoint(820, 120);
        addWaypoint(500, 150);
        addWaypoint(250, 250);
        addWaypoint(140, 420);
        addWaypoint(130, 640);
        addWaypoint(220, 830);
        addWaypoint(420, 950);
        addWaypoint(690, 960);
        addWaypoint(1020, 930);
        addWaypoint(1310, 880);
        addWaypoint(1540, 790);
        addWaypoint(1660, 580);
        addWaypoint(1640, 360);
        addWaypoint(1490, 190);
        addWaypoint(1210, 90);
        addWaypoint(850, 80);
        addWaypoint(500, 110);
        addWaypoint(220, 190);

        addBoost(1, 2, 0.30, 4);
        addBoost(15, 16, 0.24, 4);
        addBoost(38, 39, 0.38, 3);
    }

    private void addWaypoint(double x, double y) {
        waypoints.add(new double[]{x, y});
    }

    private void addBoost(int startIndex, int endIndex, double startT, int count) {
        boostStrips.add(new BoostStrip(startIndex, endIndex, startT, count));
    }

    private double distToSegment(double px, double py,
                                 double ax, double ay,
                                 double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        double len2 = dx * dx + dy * dy;
        if (len2 == 0) {
            return Math.hypot(px - ax, py - ay);
        }
        double t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / len2));
        return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }

    private double[] getWaypointTangent(int waypointIndex) {
        int count = getWaypointCount();
        double[] previous = getWaypoint((waypointIndex - 1 + count) % count);
        double[] current = getWaypoint(waypointIndex);
        double[] next = getWaypoint((waypointIndex + 1) % count);

        double inX = current[0] - previous[0];
        double inY = current[1] - previous[1];
        double outX = next[0] - current[0];
        double outY = next[1] - current[1];

        double inLen = Math.hypot(inX, inY);
        double outLen = Math.hypot(outX, outY);
        if (inLen == 0 || outLen == 0) {
            return new double[]{1.0, 0.0};
        }

        inX /= inLen;
        inY /= inLen;
        outX /= outLen;
        outY /= outLen;

        double tangentX = inX + outX;
        double tangentY = inY + outY;
        double tangentLen = Math.hypot(tangentX, tangentY);
        if (tangentLen < 0.0001) {
            tangentX = outX;
            tangentY = outY;
            tangentLen = Math.hypot(tangentX, tangentY);
        }

        return new double[]{tangentX / tangentLen, tangentY / tangentLen};
    }

    private double dot(double ax, double ay, double bx, double by) {
        return ax * bx + ay * by;
    }

    private void drawBackdrop(Graphics2D g2d) {
        switch (layout) {
            case MOON_GARDEN:
                drawMoonGardenBackdrop(g2d);
                break;
            case SUNSET_HARBOR:
                drawSunsetHarborBackdrop(g2d);
                break;
            case SKY_RIBBON:
            default:
                drawSkyRibbonBackdrop(g2d);
                break;
        }
    }

    private void drawSkyRibbonBackdrop(Graphics2D g2d) {
        GradientPaint sky = new GradientPaint(0, 0, new Color(20, 28, 72),
                                              0, WORLD_HEIGHT, new Color(16, 82, 106));
        g2d.setPaint(sky);
        g2d.fillRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        drawCloudBand(g2d, new Color(255, 255, 255, 18), 40, 140);
        g2d.setColor(new Color(20, 120, 145, 170));
        g2d.fill(new Ellipse2D.Double(1080, 150, 520, 240));
        g2d.fill(new Ellipse2D.Double(130, 120, 420, 180));

        g2d.setColor(new Color(15, 70, 90, 120));
        g2d.fill(new Ellipse2D.Double(990, 180, 430, 170));
        g2d.fill(new Ellipse2D.Double(90, 145, 340, 120));

        g2d.setColor(new Color(26, 80, 56));
        g2d.fillOval(990, 540, 280, 170);
        g2d.fillOval(180, 780, 290, 170);
        g2d.fillOval(1325, 770, 250, 145);

        g2d.setColor(new Color(50, 120, 84));
        g2d.fillOval(1020, 565, 220, 110);
        g2d.fillOval(225, 812, 210, 112);
        g2d.fillOval(1360, 798, 190, 92);

        drawGrandstand(g2d, 360, 75, 170, 70);
        drawGrandstand(g2d, 545, 92, 170, 70);
        drawGrandstand(g2d, 730, 105, 170, 70);

        g2d.setColor(new Color(255, 190, 70, 32));
        g2d.fillRect(0, WORLD_HEIGHT - 190, WORLD_WIDTH, 190);
    }

    private void drawMoonGardenBackdrop(Graphics2D g2d) {
        GradientPaint sky = new GradientPaint(0, 0, new Color(24, 20, 56),
                                              0, WORLD_HEIGHT, new Color(66, 44, 92));
        g2d.setPaint(sky);
        g2d.fillRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        g2d.setColor(new Color(246, 244, 220, 215));
        g2d.fillOval(1210, 90, 170, 170);

        g2d.setColor(new Color(255, 255, 255, 20));
        for (int i = 0; i < 42; i++) {
            int x = 90 + (i * 137) % WORLD_WIDTH;
            int y = 70 + (i * 53) % 290;
            g2d.fillOval(x, y, 3, 3);
        }

        g2d.setColor(new Color(42, 88, 72, 165));
        g2d.fillOval(155, 690, 310, 190);
        g2d.fillOval(1210, 710, 340, 190);
        g2d.fillOval(900, 500, 260, 130);

        g2d.setColor(new Color(55, 130, 116, 170));
        g2d.fillOval(1020, 610, 200, 90);
        g2d.fillOval(280, 730, 180, 78);

        g2d.setColor(new Color(100, 72, 120, 135));
        g2d.fillOval(530, 260, 220, 120);
        g2d.fillOval(1260, 285, 260, 120);

        drawLantern(g2d, 260, 250);
        drawLantern(g2d, 380, 205);
        drawLantern(g2d, 1510, 325);
        drawLantern(g2d, 1470, 745);
        drawLantern(g2d, 370, 860);
    }

    private void drawSunsetHarborBackdrop(Graphics2D g2d) {
        GradientPaint sky = new GradientPaint(0, 0, new Color(255, 140, 72),
                                              0, WORLD_HEIGHT, new Color(102, 72, 120));
        g2d.setPaint(sky);
        g2d.fillRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        g2d.setColor(new Color(248, 214, 160, 90));
        g2d.fillOval(1290, 110, 220, 220);

        g2d.setColor(new Color(40, 88, 126, 185));
        g2d.fillRect(0, 620, WORLD_WIDTH, 380);

        g2d.setColor(new Color(68, 124, 164, 140));
        for (int y = 640; y < WORLD_HEIGHT; y += 36) {
            g2d.fillRect(0, y, WORLD_WIDTH, 8);
        }

        g2d.setColor(new Color(76, 66, 60));
        g2d.fillRect(90, 530, 240, 44);
        g2d.fillRect(1280, 585, 320, 50);
        g2d.fillRect(1030, 470, 150, 38);

        g2d.setColor(new Color(230, 190, 95));
        drawCrane(g2d, 190, 360, 210, 250);
        drawCrane(g2d, 1380, 380, 180, 220);

        g2d.setColor(new Color(165, 82, 48));
        g2d.fillRect(240, 720, 120, 48);
        g2d.fillRect(370, 720, 120, 48);
        g2d.fillRect(1380, 755, 120, 48);
        g2d.fillRect(1510, 755, 120, 48);
    }

    private void drawCloudBand(Graphics2D g2d, Color color, int baseY, int spacing) {
        g2d.setColor(color);
        for (int i = -200; i < WORLD_WIDTH; i += spacing) {
            g2d.fillRoundRect(i, baseY + (i / 7) % 90, 220, 28, 28, 28);
        }
    }

    private void drawGrandstand(Graphics2D g2d, int x, int y, int w, int h) {
        g2d.setColor(new Color(30, 30, 48, 170));
        g2d.fillRoundRect(x, y, w, h, 18, 18);
        g2d.setColor(new Color(255, 216, 120, 120));
        for (int i = 10; i < w - 12; i += 22) {
            g2d.fillRoundRect(x + i, y + 16, 12, h - 30, 5, 5);
        }
    }

    private void drawLantern(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(45, 34, 52));
        g2d.fillRect(x - 3, y - 52, 6, 58);
        g2d.setColor(new Color(255, 220, 120, 130));
        g2d.fillOval(x - 18, y - 32, 36, 36);
        g2d.setColor(new Color(255, 238, 190, 190));
        g2d.fillOval(x - 9, y - 23, 18, 18);
    }

    private void drawCrane(Graphics2D g2d, int x, int y, int w, int h) {
        g2d.fillRect(x, y, 22, h);
        g2d.fillRect(x, y, w, 18);
        g2d.fillRect(x + w - 16, y + 18, 16, 90);
    }

    private GeneralPath buildPath() {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        path.moveTo((float) waypoints.get(0)[0], (float) waypoints.get(0)[1]);
        for (int i = 1; i < waypoints.size(); i++) {
            path.lineTo((float) waypoints.get(i)[0], (float) waypoints.get(i)[1]);
        }
        path.closePath();
        return path;
    }

    private void drawBoostPanels(Graphics2D g2d) {
        for (BoostStrip strip : boostStrips) {
            drawBoostStrip(g2d,
                    getWaypoint(strip.startIndex),
                    getWaypoint(strip.endIndex),
                    strip.startT,
                    strip.count);
        }
    }

    private void drawBoostStrip(Graphics2D g2d, double[] a, double[] b, double startT, int count) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double len = Math.hypot(dx, dy);
        if (len == 0) {
            return;
        }

        double ux = dx / len;
        double uy = dy / len;
        double px = -uy;
        double py = ux;

        for (int i = 0; i < count; i++) {
            double t = startT + i * 0.12;
            double cx = a[0] + dx * t;
            double cy = a[1] + dy * t;

            GeneralPath arrow = new GeneralPath();
            arrow.moveTo((float) (cx - ux * 16), (float) (cy - uy * 16));
            arrow.lineTo((float) (cx + ux * 18 + px * 17), (float) (cy + uy * 18 + py * 17));
            arrow.lineTo((float) (cx + ux * 36), (float) (cy + uy * 36));
            arrow.lineTo((float) (cx + ux * 18 - px * 17), (float) (cy + uy * 18 - py * 17));
            arrow.closePath();

            g2d.setColor(new Color(80, 255, 220, 120));
            g2d.fill(arrow);
            g2d.setColor(new Color(255, 255, 255, 110));
            g2d.draw(arrow);
        }
    }

    private void drawStartFinish(Graphics2D g2d) {
        double[] wp0 = waypoints.get(0);
        double angle = getStartAngle();
        double px = -Math.sin(angle);
        double py = Math.cos(angle);
        double halfWidth = TRACK_WIDTH / 2.0 + 6;

        int x1 = (int) Math.round(wp0[0] + px * halfWidth);
        int y1 = (int) Math.round(wp0[1] + py * halfWidth);
        int x2 = (int) Math.round(wp0[0] - px * halfWidth);
        int y2 = (int) Math.round(wp0[1] - py * halfWidth);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(14f));
        g2d.drawLine(x1, y1, x2, y2);

        g2d.setColor(Color.BLACK);
        float[] finishDash = {10f, 10f};
        g2d.setStroke(new BasicStroke(7f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f, finishDash, 0f));
        g2d.drawLine(x1, y1, x2, y2);
    }

    private Color getShoulderColor() {
        switch (layout) {
            case MOON_GARDEN:
                return new Color(216, 110, 255, 70);
            case SUNSET_HARBOR:
                return new Color(255, 120, 74, 72);
            case SKY_RIBBON:
            default:
                return new Color(255, 90, 120, 70);
        }
    }

    private Color getRimGlowColor() {
        switch (layout) {
            case MOON_GARDEN:
                return new Color(170, 210, 255, 76);
            case SUNSET_HARBOR:
                return new Color(255, 220, 180, 68);
            case SKY_RIBBON:
            default:
                return new Color(50, 235, 255, 65);
        }
    }

    private Color getRoadStartColor() {
        switch (layout) {
            case MOON_GARDEN:
                return new Color(72, 72, 92);
            case SUNSET_HARBOR:
                return new Color(74, 70, 66);
            case SKY_RIBBON:
            default:
                return new Color(70, 74, 88);
        }
    }

    private Color getRoadEndColor() {
        switch (layout) {
            case MOON_GARDEN:
                return new Color(34, 34, 52);
            case SUNSET_HARBOR:
                return new Color(44, 40, 36);
            case SKY_RIBBON:
            default:
                return new Color(36, 38, 48);
        }
    }

    private Color getLaneColor() {
        switch (layout) {
            case MOON_GARDEN:
                return new Color(220, 230, 130, 185);
            case SUNSET_HARBOR:
                return new Color(255, 240, 165, 185);
            case SKY_RIBBON:
            default:
                return new Color(255, 228, 120, 185);
        }
    }
}
