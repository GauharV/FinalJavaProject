import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Core racing simulation and event handling.
 */
public class RacingEngine {

    private final List<Racer> racers;
    private final Track track;
    private final Stack<String> eventLog;
    private int finishCount = 0;

    public RacingEngine(Track track) {
        this.track = track;
        this.racers = new ArrayList<Racer>();
        this.eventLog = new Stack<String>();
    }

    public void addRacer(Racer r) { racers.add(r); }
    public List<Racer> getRacers() { return racers; }
    public Track getTrack() { return track; }

    public String getLastEvent() {
        return eventLog.isEmpty() ? "" : eventLog.peek();
    }

    public void update(boolean accel, boolean brake,
                       boolean turnLeft, boolean turnRight,
                       boolean useAbility) {
        for (Racer racer : racers) {
            if (racer.isFinished()) {
                continue;
            }

            double previousX = racer.getX();
            double previousY = racer.getY();
            racer.tickAbilityState();

            if (racer.isPlayer()) {
                if (useAbility) {
                    tryActivateAbility(racer);
                }
                updatePlayer(racer, accel, brake, turnLeft, turnRight);
            } else {
                maybeTriggerAiAbility(racer);
                updateAI(racer);
            }

            double newX = racer.getX() + Math.cos(racer.getAngle()) * racer.getSpeed();
            double newY = racer.getY() + Math.sin(racer.getAngle()) * racer.getSpeed();

            if (!track.isOnTrack(newX, newY)) {
                racer.setSpeed(racer.getSpeed() * grassPenaltyMultiplier(racer));
            }

            racer.setX(newX);
            racer.setY(newY);

            checkWaypointProgress(racer, previousX, previousY);
        }
    }

    private void updatePlayer(Racer r, boolean accel, boolean brake,
                              boolean left, boolean right) {
        double acc = accelerationRate(r, true);
        double topSpeed = topSpeedFor(r, true);
        double handling = handlingRate(r, true);

        if (accel) {
            r.setSpeed(Math.min(r.getSpeed() + acc, topSpeed));
        } else if (brake) {
            r.setSpeed(Math.max(r.getSpeed() - acc * 2.1, -topSpeed * 0.35));
        } else {
            r.setSpeed(r.getSpeed() * coastingMultiplier(r));
        }

        double turnScale = Math.min(Math.abs(r.getSpeed()) / 2.2, 1.0);
        if (left) {
            r.setAngle(r.getAngle() - handling * turnScale);
        }
        if (right) {
            r.setAngle(r.getAngle() + handling * turnScale);
        }

        if (r.isAbilityActive()
                && r.getCharacter().getAbilityType() == BoohbahCharacter.AbilityType.RIBBON_DRIFT
                && (left || right)) {
            r.setSpeed(Math.min(r.getSpeed() + 0.02, topSpeed));
        }
    }

    private void updateAI(Racer r) {
        double[] target = track.getWaypoint(r.getCurrentWaypointIndex());
        double dx = target[0] - r.getX();
        double dy = target[1] - r.getY();
        double targetAngle = Math.atan2(dy, dx);

        double diff = normalizeAngle(targetAngle - r.getAngle());
        double handling = handlingRate(r, false);
        double steer = Math.max(-handling, Math.min(handling, diff));
        r.setAngle(r.getAngle() + steer);

        double topSpeed = topSpeedFor(r, false);
        double acc = accelerationRate(r, false);
        r.setSpeed(Math.min(r.getSpeed() + acc, topSpeed));

        if (Math.abs(diff) > 0.55) {
            r.setSpeed(r.getSpeed() * 0.992);
        }
    }

    private void tryActivateAbility(Racer racer) {
        if (!racer.canActivateAbility()) {
            return;
        }

        racer.activateAbility();
        applyActivationBurst(racer);
        eventLog.push("ABILITY: " + racer.getCharacter().getAbilityName());
    }

    private void maybeTriggerAiAbility(Racer racer) {
        if (!racer.canActivateAbility()) {
            return;
        }

        BoohbahCharacter.AbilityType type = racer.getCharacter().getAbilityType();
        double targetAngle = angleToWaypoint(racer, racer.getCurrentWaypointIndex());
        double turnDemand = Math.abs(normalizeAngle(targetAngle - racer.getAngle()));
        boolean offRoad = !track.isOnTrack(racer.getX(), racer.getY());
        boolean shouldUse = false;

        switch (type) {
            case PRISM_SHIELD:
                shouldUse = offRoad || turnDemand < 0.18;
                break;
            case COMET_DASH:
                shouldUse = turnDemand < 0.16 && racer.getSpeed() > racer.getCharacter().getTopSpeed() * 0.5;
                break;
            case MOON_LAUNCH:
                shouldUse = offRoad || racer.getSpeed() < 2.1;
                break;
            case RIBBON_DRIFT:
                shouldUse = turnDemand > 0.34 && racer.getSpeed() > 2.4;
                break;
            case WILD_CHARGE:
                shouldUse = offRoad || racer.getCurrentWaypointIndex() > track.getWaypointCount() / 2;
                break;
            default:
                break;
        }

        if (shouldUse) {
            racer.activateAbility();
            applyActivationBurst(racer);
        }
    }

    private void applyActivationBurst(Racer racer) {
        double targetTopSpeed = topSpeedFor(racer, racer.isPlayer());
        switch (racer.getCharacter().getAbilityType()) {
            case COMET_DASH:
                racer.setSpeed(Math.max(racer.getSpeed(), targetTopSpeed * 0.82));
                break;
            case MOON_LAUNCH:
                racer.setSpeed(Math.max(racer.getSpeed(), 3.6));
                break;
            case RIBBON_DRIFT:
                racer.setSpeed(Math.max(racer.getSpeed(), 2.8));
                break;
            case WILD_CHARGE:
                racer.setSpeed(Math.max(racer.getSpeed(), targetTopSpeed * 0.75));
                break;
            case PRISM_SHIELD:
            default:
                racer.setSpeed(Math.max(racer.getSpeed(), targetTopSpeed * 0.45));
                break;
        }
    }

    private double topSpeedFor(Racer racer, boolean playerPhysics) {
        double topSpeed = racer.getCharacter().getTopSpeed();
        if (!playerPhysics) {
            topSpeed *= 0.93;
        }

        if (!racer.isAbilityActive()) {
            return topSpeed;
        }

        switch (racer.getCharacter().getAbilityType()) {
            case PRISM_SHIELD:
                return topSpeed + 0.35;
            case COMET_DASH:
                return topSpeed + 2.0;
            case MOON_LAUNCH:
                return topSpeed + 0.8;
            case RIBBON_DRIFT:
                return topSpeed + 0.55;
            case WILD_CHARGE:
                return topSpeed + 1.45;
            default:
                return topSpeed;
        }
    }

    private double accelerationRate(Racer racer, boolean playerPhysics) {
        double acceleration = racer.getCharacter().getAcceleration() * (playerPhysics ? 0.18 : 0.15);
        if (!racer.isAbilityActive()) {
            return acceleration;
        }

        switch (racer.getCharacter().getAbilityType()) {
            case PRISM_SHIELD:
                return acceleration * 1.1;
            case COMET_DASH:
                return acceleration * 1.35;
            case MOON_LAUNCH:
                return acceleration * 1.95;
            case RIBBON_DRIFT:
                return acceleration * 1.18;
            case WILD_CHARGE:
                return acceleration * 1.42;
            default:
                return acceleration;
        }
    }

    private double handlingRate(Racer racer, boolean playerPhysics) {
        double handling = racer.getCharacter().getHandling() * (playerPhysics ? 0.045 : 0.039);
        if (!racer.isAbilityActive()) {
            return handling;
        }

        switch (racer.getCharacter().getAbilityType()) {
            case PRISM_SHIELD:
                return handling * 1.18;
            case COMET_DASH:
                return handling * 0.88;
            case MOON_LAUNCH:
                return handling * 1.08;
            case RIBBON_DRIFT:
                return handling * 1.85;
            case WILD_CHARGE:
                return handling * 1.14;
            default:
                return handling;
        }
    }

    private double grassPenaltyMultiplier(Racer racer) {
        if (!racer.isAbilityActive()) {
            return 0.82;
        }

        switch (racer.getCharacter().getAbilityType()) {
            case PRISM_SHIELD:
                return 0.97;
            case MOON_LAUNCH:
                return 0.89;
            case WILD_CHARGE:
                return 0.94;
            default:
                return 0.82;
        }
    }

    private double coastingMultiplier(Racer racer) {
        if (!racer.isAbilityActive()) {
            return 0.975;
        }

        switch (racer.getCharacter().getAbilityType()) {
            case PRISM_SHIELD:
                return 0.982;
            case COMET_DASH:
                return 0.989;
            case MOON_LAUNCH:
                return 0.986;
            case RIBBON_DRIFT:
                return 0.981;
            case WILD_CHARGE:
                return 0.987;
            default:
                return 0.975;
        }
    }

    private double angleToWaypoint(Racer racer, int waypointIndex) {
        double[] target = track.getWaypoint(waypointIndex);
        return Math.atan2(target[1] - racer.getY(), target[0] - racer.getX());
    }

    private double normalizeAngle(double value) {
        while (value > Math.PI) {
            value -= Math.PI * 2.0;
        }
        while (value < -Math.PI) {
            value += Math.PI * 2.0;
        }
        return value;
    }

    private void checkWaypointProgress(Racer r, double previousX, double previousY) {
        int target = r.getCurrentWaypointIndex();
        if (track.hasCrossedWaypointGate(previousX, previousY, r.getX(), r.getY(), target)) {
            r.getCheckpointQueue().offer(Integer.valueOf(target));

            if (target == 0) {
                if (r.getLap() >= Racer.TOTAL_LAPS) {
                    finishCount++;
                    r.setFinished(true);
                    r.setFinishPosition(finishCount);
                    eventLog.push("FINISH: " + r.getCharacter().getName() + " finished P" + finishCount + "!");
                } else {
                    r.incrementLap();
                    eventLog.push("LAP: " + r.getCharacter().getName() + " - Lap " + r.getLap() + "!");
                }
            }

            int next = (target + 1) % track.getWaypointCount();
            r.setCurrentWaypointIndex(next);
        }
    }

    public List<Racer> getRacePositions() {
        List<Racer> sorted = new ArrayList<Racer>(racers);
        sorted.sort((a, b) -> {
            if (a.isFinished() && b.isFinished()) {
                return a.getFinishPosition() - b.getFinishPosition();
            }
            if (a.isFinished()) {
                return -1;
            }
            if (b.isFinished()) {
                return 1;
            }
            if (a.getLap() != b.getLap()) {
                return b.getLap() - a.getLap();
            }

            int effA = effectiveWaypoint(a);
            int effB = effectiveWaypoint(b);
            if (effA != effB) {
                return effB - effA;
            }

            double distA = waypointDist(a);
            double distB = waypointDist(b);
            return Double.compare(distA, distB);
        });
        return sorted;
    }

    private int effectiveWaypoint(Racer r) {
        int idx = r.getCurrentWaypointIndex();
        return idx == 0 ? track.getWaypointCount() : idx;
    }

    private double waypointDist(Racer r) {
        double[] wp = track.getWaypoint(r.getCurrentWaypointIndex());
        return Math.hypot(r.getX() - wp[0], r.getY() - wp[1]);
    }

    public boolean isRaceOver() {
        for (Racer r : racers) {
            if (r.isPlayer() && r.isFinished()) {
                return true;
            }
        }
        return false;
    }
}
