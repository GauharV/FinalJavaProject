package com.zingzingracing.characters;

/**
 * Represents a selectable Boohbah character.
 * Uses generics for flexible stat typing.
 */
public class BoohbahCharacter {
    private final String name;
    private final String colorHex;
    private final float topSpeed;
    private final float acceleration;
    private final float handling;
    private final String description;
    private final int colorR, colorG, colorB;

    public BoohbahCharacter(String name, String colorHex, float topSpeed, float acceleration, float handling, String description, int r, int g, int b) {
        this.name = name;
        this.colorHex = colorHex;
        this.topSpeed = topSpeed;
        this.acceleration = acceleration;
        this.handling = handling;
        this.description = description;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
    }

    public String getName() { return name; }
    public String getColorHex() { return colorHex; }
    public float getTopSpeed() { return topSpeed; }
    public float getAcceleration() { return acceleration; }
    public float getHandling() { return handling; }
    public String getDescription() { return description; }
    public int getColorR() { return colorR; }
    public int getColorG() { return colorG; }
    public int getColorB() { return colorB; }

    /**
     * Factory method - creates all 5 Boohbah characters.
     * Returns them in an ArrayList (Java 2 concept).
     */
    public static java.util.ArrayList<BoohbahCharacter> createAllCharacters() {
        java.util.ArrayList<BoohbahCharacter> chars = new java.util.ArrayList<>();
        chars.add(new BoohbahCharacter("Zumbah",    "#9B59B6", 200f, 0.85f, 0.80f, "Balanced & Purple!", 155, 89, 182));
        chars.add(new BoohbahCharacter("Zing Zing", "#E74C3C", 220f, 0.70f, 0.75f, "Fast but slippery!", 231, 76, 60));
        chars.add(new BoohbahCharacter("Jumbah",    "#F39C12", 180f, 0.95f, 0.90f, "Great acceleration!", 243, 156, 18));
        chars.add(new BoohbahCharacter("Humbah",    "#27AE60", 190f, 0.90f, 0.85f, "Steady and reliable!", 39, 174, 96));
        chars.add(new BoohbahCharacter("Tombliboo", "#E91E63", 210f, 0.75f, 0.70f, "High top speed!", 233, 30, 99));
        return chars;
    }
}
