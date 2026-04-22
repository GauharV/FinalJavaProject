package com.zingbah.racing.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.zingbah.racing.characters.BoohbahCharacter;
import com.zingbah.racing.data.PowerUp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Race loop - update only, no rendering whatsoever.
 * All visuals are handled by Mode7Renderer.
 */
public class RacingEngine implements Disposable {

    public static final int TOTAL_LAPS = 5;
    private static final int RACER_COUNT = 8;

    public enum Phase { COUNTDOWN, RACING, FINISHED }

    private final Track track;
    private final List<Kart> karts = new ArrayList<>();
    private final List<AIController> aiControllers = new ArrayList<>();
    private final List<ItemBox> itemBoxes = new ArrayList<>();

    public Phase phase = Phase.COUNTDOWN;
    public float countdownTimer = 3.0f;
    private int finishCount = 0;

    private final RaceResult<Float> result = new RaceResult<>();

    public RacingEngine(BoohbahCharacter playerChar) {
        track = new Track();
        spawnGrid(playerChar);
        for (Vector3 pos : track.itemBoxPositions) {
            itemBoxes.add(new ItemBox(pos));
        }
    }

    private void spawnGrid(BoohbahCharacter playerChar) {
        float gridHeading = track.getGridHeading();

        Kart player = new Kart(playerChar, true, track.getGridPosition(0), gridHeading);
        player.waypointIndex = 1;
        karts.add(player);

        float[] skills = {1.14f, 1.09f, 1.04f, 1.00f, 0.96f, 0.91f, 0.86f};
        float[] lineOff = {-3.2f, 2.8f, -1.4f, 1.2f, -2.2f, 3.4f, 0.0f};

        List<BoohbahCharacter> roster = new ArrayList<>();
        for (BoohbahCharacter c : BoohbahCharacter.values()) {
            if (c != playerChar) {
                roster.add(c);
            }
        }
        while (roster.size() < RACER_COUNT - 1) {
            for (BoohbahCharacter c : BoohbahCharacter.values()) {
                if (roster.size() >= RACER_COUNT - 1) {
                    break;
                }
                roster.add(c);
            }
        }

        for (int slot = 1; slot < RACER_COUNT; slot++) {
            BoohbahCharacter c = roster.get(slot - 1);
            Kart ai = new Kart(c, false, track.getGridPosition(slot), gridHeading);
            ai.waypointIndex = 1;
            karts.add(ai);
            aiControllers.add(new AIController(ai, track, lineOff[slot - 1], skills[slot - 1]));
        }
    }

    public void update(float delta) {
        switch (phase) {
            case COUNTDOWN:
                countdownTimer -= delta;
                if (countdownTimer <= 0f) {
                    phase = Phase.RACING;
                }
                break;
            case RACING:
                updateRacing(delta);
                if (finishCount >= karts.size()) {
                    phase = Phase.FINISHED;
                }
                break;
            case FINISHED:
                for (Kart k : karts) {
                    k.update(delta, 0f, 0f);
                }
                break;
        }
    }

    private void updateRacing(float delta) {
        Kart player = getPlayerKart();
        float accel = 0f;
        float turn = 0f;

        if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {
            accel = 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {
            accel = -1f;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
            turn = 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
            turn = -1f;
        }

        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            tryUseItem(player);
        }

        player.update(delta, accel, turn);

        for (AIController ai : aiControllers) {
            ai.update(delta);
        }

        for (Kart k : karts) {
            if (!k.isPlayer) {
                maybeUseAiItem(k, delta);
            }

            track.enforceTrackBounds(k);
            updateProgress(k);
            checkLaps(k);
        }

        for (ItemBox box : itemBoxes) {
            box.update(delta);
            for (Kart k : karts) {
                if (box.checkPickup(k)) {
                    box.collect();
                    PowerUp pu = rollPowerUpFor(k);
                    k.itemQueue.enqueue(pu);
                }
            }
        }
    }

    private void maybeUseAiItem(Kart ai, float delta) {
        if (!ai.canUseItem()) {
            return;
        }

        PowerUp peek = ai.itemQueue.peek();
        if (peek == null) {
            return;
        }

        float useRate = peek.isDebuff() ? 1.6f : 1.1f;
        if (MathUtils.random() < delta * useRate) {
            tryUseItem(ai);
        }
    }

    private void tryUseItem(Kart user) {
        if (!user.canUseItem()) {
            return;
        }

        PowerUp powerUp = user.itemQueue.peek();
        if (powerUp == null) {
            return;
        }

        if (powerUp.isBuff() && user.activePowerUp != null) {
            return;
        }

        if (powerUp.isDebuff()) {
            Kart target = findTargetFor(user, powerUp);
            if (target == null) {
                return;
            }
            user.itemQueue.dequeue();
            target.applyDebuff(powerUp);
            user.itemUseCooldown = 1.15f;
        } else {
            user.itemQueue.dequeue();
            user.applyPowerUp(powerUp);
            user.itemUseCooldown = 0.85f;
        }
    }

    private PowerUp rollPowerUpFor(Kart kart) {
        int position = getRacePosition(kart);
        int roll = MathUtils.random(99);

        if (position >= 6) {
            if (roll < 18) return MEGA_TURBO_SAFE();
            if (roll < 36) return PowerUp.ICE_BLAST;
            if (roll < 54) return PowerUp.SHRINK_RAY;
            if (roll < 70) return PowerUp.STAR_SHIELD;
            if (roll < 84) return PowerUp.GHOST;
            return PowerUp.SPEED_BOOST;
        }

        if (position >= 3) {
            if (roll < 18) return PowerUp.SPEED_BOOST;
            if (roll < 32) return PowerUp.MEGA_TURBO;
            if (roll < 48) return PowerUp.BANANA_PEEL;
            if (roll < 64) return PowerUp.ICE_BLAST;
            if (roll < 78) return PowerUp.GHOST;
            return PowerUp.STAR_SHIELD;
        }

        if (roll < 22) return PowerUp.SPEED_BOOST;
        if (roll < 38) return PowerUp.STAR_SHIELD;
        if (roll < 52) return PowerUp.GHOST;
        if (roll < 66) return PowerUp.BANANA_PEEL;
        if (roll < 82) return PowerUp.SHRINK_RAY;
        return PowerUp.MEGA_TURBO;
    }

    private PowerUp MEGA_TURBO_SAFE() {
        return PowerUp.MEGA_TURBO;
    }

    private Kart findTargetFor(Kart user, PowerUp powerUp) {
        float userScore = (float) score(user);
        float bestScore = Float.MAX_VALUE;
        Kart best = null;
        float maxRange2 = powerUp == PowerUp.ICE_BLAST ? 42f * 42f : 34f * 34f;

        for (Kart other : karts) {
            if (other == user || other.finishedRace || other.isShielded()) {
                continue;
            }

            float dx = other.position.x - user.position.x;
            float dz = other.position.z - user.position.z;
            float dist2 = dx * dx + dz * dz;
            if (dist2 > maxRange2) {
                continue;
            }

            float deltaScore = (float) score(other) - userScore;
            float priority = dist2 - deltaScore * 520f;
            if (deltaScore < -0.25f) {
                priority += 300f;
            }

            if (priority < bestScore) {
                bestScore = priority;
                best = other;
            }
        }

        return best;
    }

    private void updateProgress(Kart k) {
        k.waypointIndex = track.advanceWaypoint(k.position, k.waypointIndex);
        float progress = track.getTrackProgress(k.position, k.waypointIndex);
        if (progress > 0.60f) {
            k.lapArmed = true;
        }
    }

    private void checkLaps(Kart k) {
        if (k.finishedRace) {
            return;
        }

        if (k.lapArmed && track.crossesFinishLineForward(k.previousPosition, k.position)) {
            k.laps++;
            k.lapArmed = false;

            if (k.laps >= TOTAL_LAPS) {
                k.finishedRace = true;
                k.finishPosition = ++finishCount;
                result.add(new RaceResult.Entry<>(finishCount, k.character, k.isPlayer, k.totalRaceTime));
            }
        }
    }

    public int getPlayerPosition() {
        return getRacePosition(getPlayerKart());
    }

    private int getRacePosition(Kart kart) {
        List<Kart> sorted = new ArrayList<>(karts);
        sorted.sort(Comparator.comparingDouble(this::score).reversed());
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i) == kart) {
                return i + 1;
            }
        }
        return 1;
    }

    private double score(Kart k) {
        return k.laps + track.getTrackProgress(k.position, k.waypointIndex);
    }

    public Kart getPlayerKart() {
        return karts.stream().filter(k -> k.isPlayer).findFirst().orElseThrow();
    }

    public List<Kart> getAllKarts() {
        return karts;
    }

    public List<ItemBox> getItemBoxes() {
        return itemBoxes;
    }

    public Track getTrack() {
        return track;
    }

    public boolean isFinished() {
        return phase == Phase.FINISHED;
    }

    public RaceResult<Float> getResult() {
        return result;
    }

    public String getCountdownText() {
        if (countdownTimer > 2f) return "3";
        if (countdownTimer > 1f) return "2";
        if (countdownTimer > 0.1f) return "1";
        return "GO!";
    }

    @Override
    public void dispose() {
        track.dispose();
    }
}
