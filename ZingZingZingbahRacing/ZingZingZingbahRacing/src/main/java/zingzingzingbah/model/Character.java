package zingzingzingbah.model;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a selectable Boohbah racing character with unique stats.
 *
 * Java 2 concepts demonstrated:
 *   - ArrayList<Character> used in getAllCharacters()
 *   - Encapsulation with private fields and public getters
 */
public class Character {

    private final String name;
    private final Color bodyColor;
    private final Color accentColor;
    private final double topSpeed;        // pixels per second
    private final double acceleration;   // pixels per second squared
    private final double handling;       // 0.0 to 1.0 (turn responsiveness)
    private final String description;

    public Character(String name, Color bodyColor, Color accentColor,
                     double topSpeed, double acceleration, double handling,
                     String description) {
        this.name        = name;
        this.bodyColor   = bodyColor;
        this.accentColor = accentColor;
        this.topSpeed    = topSpeed;
        this.acceleration = acceleration;
        this.handling    = handling;
        this.description = description;
    }

    // -----------------------------------------------------------------------
    // Factory method — returns all 5 playable Boohbahs
    // Uses ArrayList<Character> (Java 2: Lists concept)
    // -----------------------------------------------------------------------
    public static List<Character> getAllCharacters() {
        List<Character> characters = new ArrayList<>();

        characters.add(new Character(
                "Zingbah",
                Color.web("#E83535"), Color.web("#FF9999"),
                290, 110, 0.70,
                "Balanced racer"));

        characters.add(new Character(
                "Jingbah",
                Color.web("#2255EE"), Color.web("#88AAFF"),
                340, 90, 0.52,
                "Speed demon"));

        characters.add(new Character(
                "Zing Zing",
                Color.web("#E8C000"), Color.web("#FFF099"),
                260, 138, 0.74,
                "Quick off the line"));

        characters.add(new Character(
                "Wahbah",
                Color.web("#22AA33"), Color.web("#88EE99"),
                250, 100, 0.92,
                "Handles like a dream"));

        characters.add(new Character(
                "Narabah",
                Color.web("#9933DD"), Color.web("#CC88FF"),
                330, 85, 0.58,
                "Powerhouse"));

        return characters;
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------
    public String getName()        { return name; }
    public Color getBodyColor()    { return bodyColor; }
    public Color getAccentColor()  { return accentColor; }
    public double getTopSpeed()    { return topSpeed; }
    public double getAcceleration(){ return acceleration; }
    public double getHandling()    { return handling; }
    public String getDescription() { return description; }
}
