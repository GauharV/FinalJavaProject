package com.zingbah.racing.engine;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Item box — pure data, no graphics.
 * Rendered as a sprite by Mode7Renderer.
 */
public class ItemBox {

    private static final float PICKUP_RADIUS = 3.5f;
    private static final float RESPAWN_TIME  = 8f;

    public final  Vector3 position;
    public        boolean active      = true;
    private       float   respawnTimer = 0f;
    public        float   spin         = 0f;   // for visual spin, updated here

    public ItemBox(Vector3 position) {
        this.position = position.cpy();
        // Stagger initial spin so boxes don't all spin in sync
        this.spin = MathUtils.random(360f);
    }

    public void update(float delta) {
        spin += 180f * delta;
        if (!active) {
            respawnTimer -= delta;
            if (respawnTimer <= 0) active = true;
        }
    }

    public boolean checkPickup(Kart kart) {
        if (!active) return false;
        float dx = kart.position.x - position.x;
        float dz = kart.position.z - position.z;
        return dx*dx + dz*dz < PICKUP_RADIUS * PICKUP_RADIUS;
    }

    public void collect() {
        active       = false;
        respawnTimer = RESPAWN_TIME;
    }
}
