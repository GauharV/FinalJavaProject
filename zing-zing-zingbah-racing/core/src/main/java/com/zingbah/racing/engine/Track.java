package com.zingbah.racing.engine;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Long-form race circuit with no self-intersections.
 * The route is designed as a single continuous loop so AI/pathing/lap logic
 * all operate on one intended ribbon of track.
 */
public class Track implements Disposable {

    public static final float TRACK_WIDTH = 20f;
    public static final float WORLD_HALF = 150f;

    private static final float HALF_ROAD = TRACK_WIDTH / 2f;
    private static final float CURB_W = 2.8f;
    private static final int ROAD_SEGS = 300;
    private static final int WAYPOINT_STEP = 8;
    private static final int TEX = 768;
    private static final int GUIDE_STEP = 16;
    private static final int BARRIER_STEP = 12;

    private static final float GUIDE_SIDE_OFFSET = TRACK_WIDTH * 1.02f;
    private static final float BARRIER_SIDE_OFFSET = TRACK_WIDTH * 0.90f;

    private static final int COL_GRASS = rgba(0x00, 0xBB, 0x00);
    private static final int COL_GRASS2 = rgba(0x00, 0x93, 0x00);
    private static final int COL_ROAD = rgba(0x26, 0x26, 0x2C);
    private static final int COL_CENTRE = rgba(0xEE, 0xEE, 0x00);
    private static final int COL_CURB_R = rgba(0xFF, 0x10, 0x10);
    private static final int COL_CURB_W = rgba(0xF0, 0xF0, 0xF0);
    private static final int COL_FIN_BLK = rgba(0x08, 0x08, 0x08);
    private static final int COL_FIN_WHT = rgba(0xFF, 0xFF, 0xFF);

    /**
     * Long non-intersecting loop.
     * Important: this shape never crosses itself in world space.
     */
    private static final float[][] CP = {
            {-128f, -86f}, {-96f, -104f}, {-54f, -114f}, {-6f, -116f}, {42f, -112f}, {88f, -102f}, {120f, -84f},
            {136f, -54f}, {140f, -18f}, {136f, 18f}, {126f, 54f}, {112f, 86f},
            {120f, 112f}, {106f, 132f}, {74f, 138f}, {34f, 136f}, {-8f, 132f}, {-50f, 128f}, {-90f, 118f},
            {-120f, 96f}, {-134f, 64f}, {-138f, 24f}, {-132f, -10f}, {-120f, -38f},
            {-98f, -58f}, {-70f, -70f}, {-42f, -76f}, {-18f, -70f}, {4f, -56f}, {28f, -40f}, {58f, -34f},
            {88f, -42f}, {104f, -60f}, {100f, -80f}, {74f, -92f}, {34f, -94f}, {-10f, -92f}, {-56f, -90f}, {-98f, -84f}
    };

    private final List<Vector3> centerLine = new ArrayList<>();
    private final LinkedList<Vector3> waypoints = new LinkedList<>();
    private final List<GuideMarker> guideMarkers = new ArrayList<>();
    private final List<Vector3> barrierMarkers = new ArrayList<>();
    public final List<Vector3> itemBoxPositions = new ArrayList<>();

    private Texture trackTexture;

    public Track() {
        sampleCenterLine();
        buildTexture();
        buildWaypoints();
        buildItemBoxRows();
        buildGuideMarkers();
        buildBarrierMarkers();
    }

    private void sampleCenterLine() {
        Vector2[] pts = new Vector2[CP.length];
        for (int i = 0; i < CP.length; i++) {
            pts[i] = new Vector2(CP[i][0], CP[i][1]);
        }

        CatmullRomSpline<Vector2> spline = new CatmullRomSpline<>(pts, true);
        for (int i = 0; i < ROAD_SEGS; i++) {
            Vector2 p = new Vector2();
            spline.valueAt(p, (float) i / ROAD_SEGS);
            centerLine.add(new Vector3(p.x, 0f, p.y));
        }
    }

    private void buildTexture() {
        Pixmap pm = new Pixmap(TEX, TEX, Pixmap.Format.RGBA8888);
        float wf = WORLD_HALF * 2f;

        float halfRoad2 = HALF_ROAD * HALF_ROAD;
        float curbOut2 = (HALF_ROAD + CURB_W) * (HALF_ROAD + CURB_W);
        float edge2 = (HALF_ROAD + CURB_W + 3.0f) * (HALF_ROAD + CURB_W + 3.0f);

        setCol(pm, COL_GRASS);
        pm.fill();

        for (int py = 0; py < TEX; py++) {
            for (int px = 0; px < TEX; px++) {
                float wx = (px / (float) TEX) * wf - WORLD_HALF;
                float wz = WORLD_HALF - (py / (float) TEX) * wf;

                float minD2 = Float.MAX_VALUE;
                int minSeg = 0;

                for (int s = 0; s < centerLine.size(); s++) {
                    Vector3 a = centerLine.get(s);
                    Vector3 b = centerLine.get((s + 1) % centerLine.size());
                    float d2 = segDist2(wx, wz, a.x, a.z, b.x, b.z);
                    if (d2 < minD2) {
                        minD2 = d2;
                        minSeg = s;
                    }
                }

                if (minD2 < halfRoad2) {
                    boolean dash = (minSeg % 12 < 6) && (minD2 < 1.15f * 1.15f);
                    pm.drawPixel(px, py, dash ? COL_CENTRE : COL_ROAD);
                } else if (minD2 < curbOut2) {
                    boolean red = (minSeg / 5) % 2 == 0;
                    pm.drawPixel(px, py, red ? COL_CURB_R : COL_CURB_W);
                } else if (minD2 < edge2) {
                    pm.drawPixel(px, py, COL_GRASS2);
                }
            }
        }

        drawFinishLine(pm, wf);

        trackTexture = new Texture(pm);
        trackTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pm.dispose();
    }

    private void drawFinishLine(Pixmap pm, float wf) {
        Vector3 p0 = centerLine.get(0);
        Vector3 p1 = centerLine.get(1);
        float tdx = p1.x - p0.x;
        float tdz = p1.z - p0.z;
        float len = (float) Math.sqrt(tdx * tdx + tdz * tdz);
        float nx = -tdz / len;
        float nz = tdx / len;

        int n = 10;
        float tileW = TRACK_WIDTH / n;
        for (int i = 0; i < n; i++) {
            float offset = (i - n / 2f + 0.5f) * tileW;
            float cx = p0.x + nx * offset;
            float cz = p0.z + nz * offset;
            int col = (i % 2 == 0) ? COL_FIN_BLK : COL_FIN_WHT;

            for (float a = -3.5f; a <= 3.5f; a += 0.25f) {
                for (float c = -tileW / 2f; c <= tileW / 2f; c += 0.25f) {
                    float wx = cx + tdx / len * a + nx * c;
                    float wz = cz + tdz / len * a + nz * c;
                    int ppx = worldToTexX(wx, wf);
                    int ppy = worldToTexY(wz, wf);
                    if (ppx >= 0 && ppx < TEX && ppy >= 0 && ppy < TEX) {
                        pm.drawPixel(ppx, ppy, col);
                    }
                }
            }
        }
    }

    private void buildWaypoints() {
        for (int i = 0; i < centerLine.size(); i += WAYPOINT_STEP) {
            waypoints.add(centerLine.get(i).cpy());
        }
    }

    private void buildItemBoxRows() {
        int[] rowSegIndices = {20, 54, 88, 122, 156, 192, 228, 264};

        for (int segIdx : rowSegIndices) {
            if (segIdx >= centerLine.size()) {
                continue;
            }

            Vector3 cur = centerLine.get(segIdx);
            Vector3 next = centerLine.get((segIdx + 1) % centerLine.size());

            float tdx = next.x - cur.x;
            float tdz = next.z - cur.z;
            float len = (float) Math.sqrt(tdx * tdx + tdz * tdz) + 0.001f;
            float nx = -tdz / len;
            float nz = tdx / len;

            float[] offsets = {-TRACK_WIDTH * 0.34f, -TRACK_WIDTH * 0.12f, TRACK_WIDTH * 0.12f, TRACK_WIDTH * 0.34f};
            for (float off : offsets) {
                itemBoxPositions.add(new Vector3(cur.x + nx * off, 0f, cur.z + nz * off));
            }
        }
    }

    private void buildGuideMarkers() {
        for (int i = 8; i < centerLine.size(); i += GUIDE_STEP) {
            Vector3 prev = centerLine.get((i - 3 + centerLine.size()) % centerLine.size());
            Vector3 cur = centerLine.get(i);
            Vector3 next = centerLine.get((i + 3) % centerLine.size());

            float dx = next.x - prev.x;
            float dz = next.z - prev.z;
            float len = (float) Math.sqrt(dx * dx + dz * dz);
            if (len < 0.001f) {
                continue;
            }
            dx /= len;
            dz /= len;

            float nx = -dz;
            float nz = dx;
            guideMarkers.add(new GuideMarker(new Vector3(cur.x + nx * GUIDE_SIDE_OFFSET, 0f, cur.z + nz * GUIDE_SIDE_OFFSET), true));
            guideMarkers.add(new GuideMarker(new Vector3(cur.x - nx * GUIDE_SIDE_OFFSET, 0f, cur.z - nz * GUIDE_SIDE_OFFSET), false));
        }
    }

    private void buildBarrierMarkers() {
        for (int i = 0; i < centerLine.size(); i += BARRIER_STEP) {
            Vector3 prev = centerLine.get((i - 2 + centerLine.size()) % centerLine.size());
            Vector3 cur = centerLine.get(i);
            Vector3 next = centerLine.get((i + 2) % centerLine.size());

            float dx = next.x - prev.x;
            float dz = next.z - prev.z;
            float len = (float) Math.sqrt(dx * dx + dz * dz);
            if (len < 0.001f) {
                continue;
            }

            dx /= len;
            dz /= len;

            float nx = -dz;
            float nz = dx;

            barrierMarkers.add(new Vector3(cur.x + nx * BARRIER_SIDE_OFFSET, 0f, cur.z + nz * BARRIER_SIDE_OFFSET));
            barrierMarkers.add(new Vector3(cur.x - nx * BARRIER_SIDE_OFFSET, 0f, cur.z - nz * BARRIER_SIDE_OFFSET));
        }
    }

    public LinkedList<Vector3> getWaypoints() {
        return waypoints;
    }

    public int getWaypointCount() {
        return waypoints.size();
    }

    public Vector3 getWaypoint(int idx) {
        return waypoints.get(Math.floorMod(idx, waypoints.size()));
    }

    public int advanceWaypoint(Vector3 pos, int idx) {
        Vector3 t = getWaypoint(idx);
        if (Vector3.dst(pos.x, 0f, pos.z, t.x, 0f, t.z) < 12f) {
            return (idx + 1) % waypoints.size();
        }
        return idx;
    }

    public float getTrackProgress(Vector3 pos, int idx) {
        float base = (float) idx / waypoints.size();
        Vector3 curr = getWaypoint(idx);
        Vector3 next = getWaypoint(idx + 1);
        float frac = MathUtils.clamp(1f - pos.dst(next) / (curr.dst(next) + 0.01f), 0f, 1f);
        return base + frac / waypoints.size();
    }

    public float getHeadingAtWaypoint(int idx) {
        Vector3 cur = getWaypoint(idx);
        Vector3 next = getWaypoint(idx + 1);
        return MathUtils.atan2(next.z - cur.z, next.x - cur.x) * MathUtils.radiansToDegrees;
    }

    public float getDistanceFromCenter(Vector3 pos) {
        return (float) Math.sqrt(findClosestSample(pos.x, pos.z).d2);
    }

    public Vector3 getClosestCenterPoint(Vector3 pos) {
        ClosestSample sample = findClosestSample(pos.x, pos.z);
        return new Vector3(sample.nearX, 0f, sample.nearZ);
    }

    public Vector3 getGridPosition(int slot) {
        int wpIdx = waypoints.size() - 1 - slot;
        Vector3 wp = getWaypoint(wpIdx).cpy();
        float tanH = getHeadingAtWaypoint(wpIdx);
        float nx = -MathUtils.sinDeg(tanH);
        float nz = MathUtils.cosDeg(tanH);
        float rowBack = (slot / 2) * 5.8f;
        float side = (slot % 2 == 0) ? -1f : 1f;

        wp.x += nx * side * 4.8f - MathUtils.cosDeg(tanH) * rowBack;
        wp.z += nz * side * 4.8f - MathUtils.sinDeg(tanH) * rowBack;
        return wp;
    }

    public float getGridHeading() {
        return getHeadingAtWaypoint(0);
    }

    public boolean crossesFinishLineForward(Vector3 prevPos, Vector3 currentPos) {
        Vector3 start = centerLine.get(0);
        Vector3 ahead = centerLine.get(1);

        float tdx = ahead.x - start.x;
        float tdz = ahead.z - start.z;
        float len = (float) Math.sqrt(tdx * tdx + tdz * tdz) + 0.0001f;
        float tx = tdx / len;
        float tz = tdz / len;
        float nx = -tz;
        float nz = tx;

        float prevLong = (prevPos.x - start.x) * tx + (prevPos.z - start.z) * tz;
        float currLong = (currentPos.x - start.x) * tx + (currentPos.z - start.z) * tz;
        float prevLat = (prevPos.x - start.x) * nx + (prevPos.z - start.z) * nz;
        float currLat = (currentPos.x - start.x) * nx + (currentPos.z - start.z) * nz;

        boolean withinStripe = Math.abs(prevLat) <= HALF_ROAD + 3f || Math.abs(currLat) <= HALF_ROAD + 3f;
        return withinStripe && prevLong <= 0f && currLong > 0f;
    }

    public List<GuideMarker> getGuideMarkers() {
        return guideMarkers;
    }

    public List<Vector3> getBarrierMarkers() {
        return barrierMarkers;
    }

    public boolean enforceTrackBounds(Kart kart) {
        if (kart.activePowerUp == com.zingbah.racing.data.PowerUp.GHOST) {
            return false;
        }

        ClosestSample sample = findClosestSample(kart.position.x, kart.position.z);
        float limit = HALF_ROAD + 2.8f;
        if (sample.d2 > limit * limit) {
            float d = (float) Math.sqrt(sample.d2);
            float push = d - (HALF_ROAD - 0.5f);
            float bestNx = sample.nearX - kart.position.x;
            float bestNz = sample.nearZ - kart.position.z;
            float len = (float) Math.sqrt(bestNx * bestNx + bestNz * bestNz) + 0.001f;

            kart.position.x += (bestNx / len) * push;
            kart.position.z += (bestNz / len) * push;
            kart.speed *= 0.45f;
            return true;
        }
        return false;
    }

    public Texture getTexture() {
        return trackTexture;
    }

    private ClosestSample findClosestSample(float px, float pz) {
        ClosestSample best = new ClosestSample();
        best.d2 = Float.MAX_VALUE;

        for (int s = 0; s < centerLine.size(); s++) {
            Vector3 a = centerLine.get(s);
            Vector3 b = centerLine.get((s + 1) % centerLine.size());
            float t = segT(px, pz, a.x, a.z, b.x, b.z);
            float nearX = a.x + t * (b.x - a.x);
            float nearZ = a.z + t * (b.z - a.z);
            float dx = nearX - px;
            float dz = nearZ - pz;
            float d2 = dx * dx + dz * dz;

            if (d2 < best.d2) {
                best.d2 = d2;
                best.nearX = nearX;
                best.nearZ = nearZ;
            }
        }

        return best;
    }

    private static float segDist2(float px, float pz, float ax, float az, float bx, float bz) {
        float t = segT(px, pz, ax, az, bx, bz);
        float nx = ax + t * (bx - ax) - px;
        float nz = az + t * (bz - az) - pz;
        return nx * nx + nz * nz;
    }

    private static float segT(float px, float pz, float ax, float az, float bx, float bz) {
        float dx = bx - ax;
        float dz = bz - az;
        float len2 = dx * dx + dz * dz;
        if (len2 < 1e-6f) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, ((px - ax) * dx + (pz - az) * dz) / len2));
    }

    private static int worldToTexX(float wx, float wf) {
        return (int) ((wx + WORLD_HALF) / wf * TEX);
    }

    private static int worldToTexY(float wz, float wf) {
        return (int) ((WORLD_HALF - wz) / wf * TEX);
    }

    private static float toColorFloat(int color, int shift) {
        return ((color >> shift) & 0xFF) / 255f;
    }

    private static void setCol(Pixmap pm, int col) {
        pm.setColor(toColorFloat(col, 24), toColorFloat(col, 16), toColorFloat(col, 8), toColorFloat(col, 0));
    }

    private static int rgba(int r, int g, int b) {
        return (r << 24) | (g << 16) | (b << 8) | 0xFF;
    }

    @Override
    public void dispose() {
        if (trackTexture != null) {
            trackTexture.dispose();
        }
    }

    private static class ClosestSample {
        float d2;
        float nearX;
        float nearZ;
    }

    public static class GuideMarker {
        public final Vector3 position;
        public final boolean pointRight;

        public GuideMarker(Vector3 position, boolean pointRight) {
            this.position = position;
            this.pointRight = pointRight;
        }
    }
}
