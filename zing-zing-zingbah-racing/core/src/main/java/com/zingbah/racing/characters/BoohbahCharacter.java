package com.zingbah.racing.characters;

import com.badlogic.gdx.graphics.Color;

/**
 * The five playable Boohbah characters with unique stats.
 * Turn speed has been tuned to feel responsive without spinning out.
 */
public enum BoohbahCharacter {

    // Pure NES palette colors — no blended pastels
    ZUMBAH   ("Zumbah",    new Color(0.98f, 0.84f, 0.00f, 1f), 3, 3, 3, "Balanced — great all-rounder."),
    ZING_ZING("Zing Zing", new Color(0.00f, 0.44f, 1.00f, 1f), 5, 2, 2, "Lightning fast but loose on corners!"),
    JUMBAH   ("Jumbah",    new Color(0.00f, 0.67f, 0.08f, 1f), 4, 2, 3, "Big and green — builds up speed."),
    HUMBAH   ("Humbah",    new Color(0.94f, 0.00f, 0.55f, 1f), 2, 5, 5, "Tiny and nimble — sticks every apex."),
    TODAH    ("Todah",     new Color(0.98f, 0.44f, 0.00f, 1f), 3, 4, 3, "Orange heat off the line.");

    public final String displayName;
    public final Color  color;
    public final String tagline;

    // Stat ratings (1–5)
    public final int topSpeed;
    public final int acceleration;
    public final int handling;

    // Physics values
    public final float maxSpeed;    // units/sec
    public final float accelForce;  // units/sec²
    public final float turnSpeed;   // degrees/sec  — kept sane (44–68°/sec)

    BoohbahCharacter(String name, Color color,
                     int topSpeed, int acceleration, int handling, String tagline) {
        this.displayName  = name;
        this.color        = color;
        this.topSpeed     = topSpeed;
        this.acceleration = acceleration;
        this.handling     = handling;
        this.tagline      = tagline;

        this.maxSpeed   = 14f + topSpeed     * 3.2f;   // 17.2 – 30.0 u/s
        this.accelForce = 5f  + acceleration * 2.2f;   //  7.2 – 16.0 u/s²
        // Turn speed intentionally conservative — max ~68°/sec so karts don't spin out
        this.turnSpeed  = 44f + handling * 4.8f;       // 48.8 – 68.0 °/s
    }
}
