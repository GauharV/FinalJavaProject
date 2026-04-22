package com.zingbah.racing.engine;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Drives an AI kart by chasing waypoints around the track.
 * Tuned for a fuller race pace with better lookahead, recovery, and cornering.
 */
public class AIController {

    private static final float BASE_REACTION_SLOP = 8f;
    private static final float RECOVERY_DISTANCE = Track.TRACK_WIDTH * 0.46f;

    private final Kart kart;
    private final Track track;
    private final float lineOffset;
    private final float skill;

    public AIController(Kart kart, Track track, float lineOffset, float skill) {
        this.kart = kart;
        this.track = track;
        this.lineOffset = lineOffset;
        this.skill = MathUtils.clamp(skill, 0.72f, 1.18f);
    }

    public void update(float delta) {
        if (kart.finishedRace) {
            return;
        }

        kart.waypointIndex = track.advanceWaypoint(kart.position, kart.waypointIndex);

        float distFromCenter = track.getDistanceFromCenter(kart.position);
        Vector3 target = distFromCenter > RECOVERY_DISTANCE ? getRecoveryTarget() : getTargetWithOffset();

        float angleNow = angleToTarget(target);
        float upcomingTurn = getUpcomingTurnSeverity();
        float accel = computeAccel(angleNow, upcomingTurn, distFromCenter);
        float turn = computeTurn(angleNow, distFromCenter);

        kart.update(delta, accel, turn);
    }

    private Vector3 getTargetWithOffset() {
        int lookahead = getLookaheadSteps();
        int targetIdx = (kart.waypointIndex + lookahead) % track.getWaypointCount();
        Vector3 wp = track.getWaypoint(targetIdx).cpy();

        Vector3 prev = track.getWaypoint((targetIdx - 1 + track.getWaypointCount()) % track.getWaypointCount());
        Vector3 next = track.getWaypoint((targetIdx + 1) % track.getWaypointCount());
        Vector3 dir = next.cpy().sub(prev).nor();
        Vector3 perp = new Vector3(dir.z, 0f, -dir.x);

        float lineBias = MathUtils.lerp(0.45f, 1.20f, (skill - 0.72f) / 0.46f);
        wp.add(perp.scl(lineOffset * lineBias));
        return wp;
    }

    private Vector3 getRecoveryTarget() {
        Vector3 nearest = track.getClosestCenterPoint(kart.position);
        Vector3 next = track.getWaypoint((kart.waypointIndex + 2) % track.getWaypointCount()).cpy();
        Vector3 forward = next.sub(nearest).nor().scl(12f);
        return nearest.add(forward);
    }

    private int getLookaheadSteps() {
        float speedRatio = MathUtils.clamp(Math.abs(kart.speed) / Math.max(1f, kart.character.maxSpeed), 0f, 1f);
        return 2 + MathUtils.floor(speedRatio * 4f + (skill - 0.72f) * 4f);
    }

    private float getUpcomingTurnSeverity() {
        float currentHeading = track.getHeadingAtWaypoint(kart.waypointIndex);
        float strongest = 0f;
        for (int i = 1; i <= 5; i++) {
            float futureHeading = track.getHeadingAtWaypoint(kart.waypointIndex + i);
            strongest = Math.max(strongest, Math.abs(normalizeAngle(futureHeading - currentHeading)));
        }
        return strongest;
    }

    private float computeAccel(float angleNow, float upcomingTurn, float distFromCenter) {
        float curveSlow = MathUtils.clamp(upcomingTurn / 85f, 0f, 1f);
        float steeringSlow = MathUtils.clamp(Math.abs(angleNow) / 65f, 0f, 1f);
        float recoverySlow = MathUtils.clamp((distFromCenter - Track.TRACK_WIDTH * 0.20f) / (Track.TRACK_WIDTH * 0.34f), 0f, 1f);

        float targetFraction = 1.06f;
        targetFraction -= curveSlow * 0.48f;
        targetFraction -= steeringSlow * 0.22f;
        targetFraction -= recoverySlow * 0.24f;
        targetFraction *= skill;
        targetFraction = MathUtils.clamp(targetFraction, 0.45f, 1.16f);

        float targetSpeed = kart.character.maxSpeed * targetFraction;
        if (kart.speed > targetSpeed + 2.8f) {
            return -0.60f;
        }
        if (kart.speed < targetSpeed - 2.0f) {
            return 1f;
        }
        return 0.55f;
    }

    private float computeTurn(float angle, float distFromCenter) {
        float recoveryBoost = MathUtils.clamp(distFromCenter / RECOVERY_DISTANCE, 0f, 1f);
        float slop = MathUtils.lerp(BASE_REACTION_SLOP, 2f, recoveryBoost);
        if (Math.abs(angle) < slop) {
            return 0f;
        }

        float strength = MathUtils.clamp(angle / 34f, -1f, 1f);
        strength *= MathUtils.lerp(0.95f, 1.18f, (skill - 0.72f) / 0.46f);
        strength *= 1f + recoveryBoost * 0.40f;
        return MathUtils.clamp(strength, -1f, 1f);
    }

    private float angleToTarget(Vector3 target) {
        float dx = target.x - kart.position.x;
        float dz = target.z - kart.position.z;
        float desired = MathUtils.atan2(dz, dx) * MathUtils.radiansToDegrees;
        return normalizeAngle(desired - kart.heading);
    }

    private float normalizeAngle(float angle) {
        while (angle > 180f) {
            angle -= 360f;
        }
        while (angle < -180f) {
            angle += 360f;
        }
        return angle;
    }
}
