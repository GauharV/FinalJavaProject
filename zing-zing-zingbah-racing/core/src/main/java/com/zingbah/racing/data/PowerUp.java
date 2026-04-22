package com.zingbah.racing.data;

/**
 * All collectable power-ups.
 * Each has a display name, speed multiplier, duration, and a ScreenEffect
 * that the renderer uses while the effect is active.
 */
public enum PowerUp {

    SPEED_BOOST ("SPEED BOOST!",  1.55f, 3.4f, ScreenEffect.ORANGE_TRAIL),
    MEGA_TURBO  ("MEGA TURBO!!",  1.95f, 2.4f, ScreenEffect.RED_FLASH),
    STAR_SHIELD ("STAR SHIELD",   1.00f, 5.5f, ScreenEffect.GOLD_SHIMMER),
    SHRINK_RAY  ("SHRINK RAY",    0.68f, 4.5f, ScreenEffect.PURPLE_TINT),
    BANANA_PEEL ("BANANA PEEL",   0.52f, 1.9f, ScreenEffect.YELLOW_SPIN),
    ICE_BLAST   ("ICE BLAST",     0.35f, 2.8f, ScreenEffect.BLUE_FREEZE),
    GHOST       ("GHOST",         1.22f, 5.0f, ScreenEffect.WHITE_GLOW);

    public enum ScreenEffect {
        NONE,
        ORANGE_TRAIL,
        RED_FLASH,
        GOLD_SHIMMER,
        PURPLE_TINT,
        YELLOW_SPIN,
        BLUE_FREEZE,
        WHITE_GLOW
    }

    public final String displayName;
    public final float speedMultiplier;
    public final float duration;
    public final ScreenEffect effect;

    PowerUp(String displayName, float speedMultiplier, float duration, ScreenEffect effect) {
        this.displayName = displayName;
        this.speedMultiplier = speedMultiplier;
        this.duration = duration;
        this.effect = effect;
    }

    public boolean isDebuff() {
        return this == SHRINK_RAY || this == BANANA_PEEL || this == ICE_BLAST;
    }

    public boolean isBuff() {
        return !isDebuff();
    }
}
