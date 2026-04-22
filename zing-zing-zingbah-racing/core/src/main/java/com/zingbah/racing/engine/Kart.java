package com.zingbah.racing.engine;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.zingbah.racing.characters.BoohbahCharacter;
import com.zingbah.racing.data.ItemQueue;
import com.zingbah.racing.data.PowerUp;

/**
 * Pure physics kart - no graphics whatsoever.
 * Rendering is handled by Mode7Renderer using screen-space sprites.
 */
public class Kart {

    public final Vector3 position = new Vector3();
    public final Vector3 previousPosition = new Vector3();
    public float heading = 0f;
    public float speed = 0f;

    public int laps = 0;
    public int waypointIndex = 0;
    public boolean finishedRace = false;
    public int finishPosition = 0;
    public float totalRaceTime = 0f;
    public boolean lapArmed = false;

    public final BoohbahCharacter character;
    public final boolean isPlayer;

    public final ItemQueue<PowerUp> itemQueue = new ItemQueue<>();
    public PowerUp activePowerUp = null;
    public float powerUpTimer = 0f;
    public boolean isShrunk = false;
    public float itemUseCooldown = 0f;

    private static final float DRAG = 4.5f;
    private static final float BRAKE_DRAG = 14f;

    public Kart(BoohbahCharacter character, boolean isPlayer,
                Vector3 startPos, float startHeading) {
        this.character = character;
        this.isPlayer = isPlayer;
        this.position.set(startPos);
        this.previousPosition.set(startPos);
        this.heading = startHeading;
    }

    public void update(float delta, float accel, float turn) {
        previousPosition.set(position);
        itemUseCooldown = Math.max(0f, itemUseCooldown - delta);

        if (finishedRace) {
            speed = MathUtils.lerp(speed, 0f, 4f * delta);
            return;
        }

        totalRaceTime += delta;

        float speedMult = isShrunk ? 0.5f : 1f;
        if (activePowerUp != null) {
            powerUpTimer -= delta;
            if (activePowerUp != PowerUp.SHRINK_RAY
                    && activePowerUp != PowerUp.BANANA_PEEL
                    && activePowerUp != PowerUp.ICE_BLAST) {
                speedMult = activePowerUp.speedMultiplier;
            }
            if (powerUpTimer <= 0f) {
                activePowerUp = null;
                isShrunk = false;
                speedMult = 1f;
            }
        }

        float maxSpd = character.maxSpeed * speedMult;

        if (accel > 0.01f) {
            speed += accel * character.accelForce * delta;
        } else if (accel < -0.01f) {
            if (speed > 0.3f) {
                speed -= BRAKE_DRAG * delta;
            } else {
                speed += accel * character.accelForce * 0.5f * delta;
            }
        } else {
            if (speed > 0f) {
                speed = Math.max(0f, speed - DRAG * delta);
            } else if (speed < 0f) {
                speed = Math.min(0f, speed + DRAG * delta);
            }
        }
        speed = MathUtils.clamp(speed, -5f, maxSpd);

        if (Math.abs(speed) > 0.5f && Math.abs(turn) > 0.01f) {
            float ratio = Math.abs(speed) / Math.max(1f, maxSpd);
            float factor = 1f - 0.25f * ratio;
            heading += turn * character.turnSpeed * factor * (speed < 0 ? -1 : 1) * delta;
        }

        float rad = heading * MathUtils.degreesToRadians;
        position.x += MathUtils.cos(rad) * speed * delta;
        position.z += MathUtils.sin(rad) * speed * delta;
        position.y = 0f;
    }

    public void useItem() {
        if (!itemQueue.isEmpty() && activePowerUp == null) {
            applyPowerUp(itemQueue.dequeue());
        }
    }

    public void applyPowerUp(PowerUp p) {
        activePowerUp = p;
        powerUpTimer = p.duration;
        isShrunk = (p == PowerUp.SHRINK_RAY);
    }

    public void applyDebuff(PowerUp p) {
        if (isShielded()) {
            return;
        }

        activePowerUp = p;
        powerUpTimer = p.duration;
        isShrunk = (p == PowerUp.SHRINK_RAY);

        if (p == PowerUp.BANANA_PEEL) {
            speed = Math.min(speed, character.maxSpeed * 0.28f);
        } else if (p == PowerUp.ICE_BLAST) {
            speed = Math.min(speed, character.maxSpeed * 0.18f);
        }
    }

    public boolean isShielded() {
        return activePowerUp == PowerUp.STAR_SHIELD || activePowerUp == PowerUp.GHOST;
    }

    public boolean canUseItem() {
        return itemUseCooldown <= 0f && !itemQueue.isEmpty();
    }

    public Vector3 getForward() {
        return new Vector3(MathUtils.cosDeg(heading), 0f, MathUtils.sinDeg(heading));
    }
}
